package com.julio.carrerasatletismo.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.julio.carrerasatletismo.data.local.AppDatabase
import com.julio.carrerasatletismo.data.local.CarreraEntity
import com.julio.carrerasatletismo.data.local.CategoriaEntity
import com.julio.carrerasatletismo.data.local.CiudadEntity
import com.julio.carrerasatletismo.data.local.CorredorEntity
import com.julio.carrerasatletismo.data.local.InscripcionEntity
import com.julio.carrerasatletismo.data.local.PaisEntity
import com.julio.carrerasatletismo.data.local.ResultadoEntity
import com.julio.carrerasatletismo.data.local.dao.CarreraDao
import com.julio.carrerasatletismo.data.local.dao.CategoriaDao
import com.julio.carrerasatletismo.data.local.dao.CorredorDao
import com.julio.carrerasatletismo.data.local.dao.InscripcionDao
import com.julio.carrerasatletismo.data.mapper.toDomain
import com.julio.carrerasatletismo.data.mapper.toEntity
import com.julio.carrerasatletismo.data.remote.RunDidaApi
import com.julio.carrerasatletismo.data.remote.comoTexto
import com.julio.carrerasatletismo.domain.model.Carrera
import com.julio.carrerasatletismo.domain.model.Categoria
import com.julio.carrerasatletismo.domain.model.Corredor
import com.julio.carrerasatletismo.domain.model.CorredorConCarreras
import com.julio.carrerasatletismo.domain.model.ErrorDominio
import com.julio.carrerasatletismo.domain.model.InscritoEnCarrera
import com.julio.carrerasatletismo.domain.repository.CarrerasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * PATRÓN REPOSITORY – implementación (capa data).
 * Une la fuente local (Room) y la remota (Retrofit) y entrega modelos de dominio usando los mappers.
 *
 * INYECCIÓN DE DEPENDENCIAS: @Inject constructor le dice a Hilt cómo crear esta clase;
 * Hilt le pasa la base de datos, los 4 DAO y la API (vienen de di/ModuloDatos.kt).
 * @Singleton: una sola instancia compartida por toda la app.
 *
 * SOLID – L (Sustitución de Liskov): esta clase cumple TODO el contrato de CarrerasRepository,
 * así que puede usarse en cualquier lugar donde se pida la interfaz (o cambiarse por una
 * implementación falsa para pruebas) sin que los ViewModels noten la diferencia.
 */
@Singleton
class CarrerasRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val carreraDao: CarreraDao,
    private val corredorDao: CorredorDao,
    private val categoriaDao: CategoriaDao,
    private val inscripcionDao: InscripcionDao,
    private val api: RunDidaApi
) : CarrerasRepository {

    // =====================================================================
    // CARRERAS
    // =====================================================================

    override fun observarCarreras(busqueda: String): Flow<List<Carrera>> =
        carreraDao.buscar(busqueda.trim()).map { lista -> lista.map { it.toDomain() } }

    override fun observarCarrera(id: String): Flow<Carrera?> =
        carreraDao.observarPorId(id).map { it?.toDomain() }

    override suspend fun obtenerCarrera(id: String): Carrera? =
        carreraDao.obtenerVistaPorId(id)?.toDomain()

    override suspend fun agregarCarrera(carrera: Carrera): String = db.withTransaction {
        if (carreraDao.contarDuplicadas(carrera.nombre, carrera.fecha, "") > 0) {
            throw ErrorDominio.NombreDuplicado(carrera.nombre)
        }
        val id = Carrera.PREFIJO_LOCAL + UUID.randomUUID().toString()
        val idCiudad = obtenerIdCiudad(carrera.ciudad, carrera.pais)
        carreraDao.insertar(carrera.copy(idCarrera = id).toEntity(idCiudad))
        id
    }

    override suspend fun actualizarCarrera(carrera: Carrera) = db.withTransaction {
        carreraDao.obtenerPorId(carrera.idCarrera) ?: throw ErrorDominio.NoEncontrado()
        if (carreraDao.contarDuplicadas(carrera.nombre, carrera.fecha, carrera.idCarrera) > 0) {
            throw ErrorDominio.NombreDuplicado(carrera.nombre)
        }
        val idCiudad = obtenerIdCiudad(carrera.ciudad, carrera.pais)
        carreraDao.actualizar(carrera.toEntity(idCiudad))
    }

    /** Al borrar la carrera, Room/SQLite borra en CASCADA sus inscripciones y resultados. */
    override suspend fun eliminarCarrera(id: String) {
        val entidad = carreraDao.obtenerPorId(id) ?: throw ErrorDominio.NoEncontrado()
        carreraDao.eliminar(entidad)
    }

    override suspend fun hayCarreras(): Boolean = carreraDao.contar() > 0

    /** Descarga las maratones de la API y las guarda en Room. Sin internet lanza SinConexion. */
    override suspend fun sincronizarCarreras(): Int {
        val respuesta = try {
            api.obtenerMaratones()
        } catch (e: IOException) {
            throw ErrorDominio.SinConexion()
        }
        val lista = respuesta.active.orEmpty().mapNotNull { dto ->
            val id = dto.id ?: return@mapNotNull null
            id to dto
        }
        db.withTransaction {
            lista.forEach { (id, dto) ->
                val idCiudad = obtenerIdCiudad(dto.city.orEmpty(), dto.country.orEmpty())
                // IGNORE: si la carrera ya estaba guardada no se duplica ni se pierden ediciones del usuario
                carreraDao.insertarSiNoExiste(
                    CarreraEntity(
                        idCarrera = id,
                        nombre = dto.name.comoTexto().ifBlank { id },
                        fecha = dto.date.orEmpty(),
                        idCiudad = idCiudad,
                        url = dto.url.orEmpty()
                    )
                )
            }
        }
        return lista.size
    }

    /** Completa clima y recorrido desde la API (solo carreras que vienen de la API). */
    override suspend fun cargarDetalleRemoto(id: String) {
        if (id.startsWith(Carrera.PREFIJO_LOCAL)) return
        val detalle = try {
            api.obtenerMaraton(id).marathon ?: return
        } catch (e: IOException) {
            throw ErrorDominio.SinConexion()
        }
        carreraDao.actualizarDetalle(
            id = id,
            tipo = detalle.course?.type,
            desnivel = detalle.course?.elevationGain?.roundToInt(),
            terreno = detalle.course?.terrain,
            temperatura = detalle.weather?.avgTempC,
            humedad = detalle.weather?.humidity?.roundToInt(),
            condiciones = detalle.weather?.conditions
        )
    }

    /** Busca la ciudad (y su país) o la crea si no existe. */
    private suspend fun obtenerIdCiudad(ciudad: String, pais: String): Long? {
        val nombreCiudad = ciudad.trim()
        val nombrePais = pais.trim()
        if (nombreCiudad.isEmpty() && nombrePais.isEmpty()) return null

        val idPais = if (nombrePais.isEmpty()) null
        else carreraDao.idPais(nombrePais) ?: carreraDao.insertarPais(PaisEntity(nombre = nombrePais))

        return carreraDao.idCiudad(nombreCiudad, idPais)
            ?: carreraDao.insertarCiudad(CiudadEntity(nombre = nombreCiudad, idPais = idPais))
    }

    // =====================================================================
    // CORREDORES
    // =====================================================================

    override fun observarCorredores(): Flow<List<Corredor>> =
        corredorDao.observarTodos().map { lista -> lista.map { it.toDomain() } }

    override fun observarCorredoresConCarreras(): Flow<List<CorredorConCarreras>> =
        corredorDao.observarConCarreras().map { lista -> lista.map { it.toDomain() } }

    override suspend fun obtenerCorredor(id: Long): Corredor? =
        corredorDao.obtenerVistaPorId(id)?.toDomain()

    /** La categoría se asigna sola según la edad (tabla CATEGORIA). */
    override suspend fun agregarCorredor(nombre: String, edad: Int) {
        if (corredorDao.contarPorNombre(nombre, -1L) > 0) throw ErrorDominio.NombreDuplicado(nombre)
        val idCategoria = categoriaDao.idParaEdad(edad) ?: throw ErrorDominio.SinCategoriaParaEdad(edad)
        corredorDao.insertar(CorredorEntity(nombre = nombre.trim(), edad = edad, idCategoria = idCategoria))
    }

    override suspend fun actualizarCorredor(id: Long, nombre: String, edad: Int) {
        val actual = corredorDao.obtenerPorId(id) ?: throw ErrorDominio.NoEncontrado()
        if (corredorDao.contarPorNombre(nombre, id) > 0) throw ErrorDominio.NombreDuplicado(nombre)
        val idCategoria = categoriaDao.idParaEdad(edad) ?: throw ErrorDominio.SinCategoriaParaEdad(edad)
        corredorDao.actualizar(actual.copy(nombre = nombre.trim(), edad = edad, idCategoria = idCategoria))
    }

    /** Al borrar el corredor se borran en CASCADA sus inscripciones y resultados. */
    override suspend fun eliminarCorredor(id: Long) {
        val actual = corredorDao.obtenerPorId(id) ?: throw ErrorDominio.NoEncontrado()
        corredorDao.eliminar(actual)
    }

    // =====================================================================
    // CATEGORÍAS
    // =====================================================================

    override fun observarCategorias(): Flow<List<Categoria>> =
        categoriaDao.observarConCorredores().map { lista -> lista.map { it.toDomain() } }

    override suspend fun obtenerCategoria(id: Long): Categoria? =
        categoriaDao.obtenerPorId(id)?.toDomain()

    override suspend fun agregarCategoria(nombre: String, edadMin: Int, edadMax: Int) {
        try {
            categoriaDao.insertar(CategoriaEntity(nombre = nombre.trim(), edadMin = edadMin, edadMax = edadMax))
        } catch (e: SQLiteConstraintException) {
            // El índice UNIQUE de "nombre" rechazó el registro → excepción propia con mensaje claro
            throw ErrorDominio.NombreDuplicado(nombre.trim())
        }
    }

    override suspend fun actualizarCategoria(categoria: Categoria) {
        categoriaDao.obtenerPorId(categoria.idCategoria) ?: throw ErrorDominio.NoEncontrado()
        try {
            categoriaDao.actualizar(categoria.copy(nombre = categoria.nombre.trim()).toEntity())
        } catch (e: SQLiteConstraintException) {
            throw ErrorDominio.NombreDuplicado(categoria.nombre.trim())
        }
    }

    /** La llave foránea es RESTRICT: si la categoría tiene corredores, SQLite no deja borrarla. */
    override suspend fun eliminarCategoria(id: Long) {
        val actual = categoriaDao.obtenerPorId(id) ?: throw ErrorDominio.NoEncontrado()
        try {
            categoriaDao.eliminar(actual)
        } catch (e: SQLiteConstraintException) {
            throw ErrorDominio.CategoriaEnUso(actual.nombre)
        }
    }

    // =====================================================================
    // INSCRIPCIONES Y RESULTADOS
    // =====================================================================

    override fun observarInscritos(idCarrera: String): Flow<List<InscritoEnCarrera>> =
        inscripcionDao.observarInscritos(idCarrera).map { lista -> lista.map { it.toDomain() } }

    override suspend fun inscribir(idCorredor: Long, idCarrera: String, dorsal: Int, tiempoObjetivo: String) {
        try {
            db.withTransaction {
                if (inscripcionDao.obtener(idCorredor, idCarrera) != null) throw ErrorDominio.YaInscrito()
                if (inscripcionDao.duenoDelDorsal(idCarrera, dorsal) != null) throw ErrorDominio.DorsalOcupado(dorsal)
                inscripcionDao.insertar(InscripcionEntity(idCorredor, idCarrera, dorsal, tiempoObjetivo))
            }
        } catch (e: SQLiteConstraintException) {
            throw ErrorDominio.DorsalOcupado(dorsal)
        }
    }

    override suspend fun actualizarInscripcion(
        idCorredor: Long, idCarrera: String, dorsal: Int, tiempoObjetivo: String
    ) {
        try {
            db.withTransaction {
                val actual = inscripcionDao.obtener(idCorredor, idCarrera) ?: throw ErrorDominio.NoEncontrado()
                val dueno = inscripcionDao.duenoDelDorsal(idCarrera, dorsal)
                if (dueno != null && dueno != idCorredor) throw ErrorDominio.DorsalOcupado(dorsal)
                inscripcionDao.actualizar(actual.copy(dorsal = dorsal, tiempoObjetivo = tiempoObjetivo))
            }
        } catch (e: SQLiteConstraintException) {
            throw ErrorDominio.DorsalOcupado(dorsal)
        }
    }

    override suspend fun quitarInscripcion(idCorredor: Long, idCarrera: String) {
        val actual = inscripcionDao.obtener(idCorredor, idCarrera) ?: throw ErrorDominio.NoEncontrado()
        inscripcionDao.eliminar(actual)
    }

    override suspend fun guardarResultado(idCorredor: Long, idCarrera: String, tiempoReal: String) =
        inscripcionDao.guardarResultado(ResultadoEntity(idCorredor, idCarrera, tiempoReal))

    override suspend fun borrarResultado(idCorredor: Long, idCarrera: String) =
        inscripcionDao.eliminarResultado(ResultadoEntity(idCorredor, idCarrera, ""))
}
