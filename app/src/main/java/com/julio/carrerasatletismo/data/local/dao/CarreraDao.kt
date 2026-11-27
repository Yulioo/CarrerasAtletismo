package com.julio.carrerasatletismo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.julio.carrerasatletismo.data.local.CarreraEntity
import com.julio.carrerasatletismo.data.local.CarreraVista
import com.julio.carrerasatletismo.data.local.CiudadEntity
import com.julio.carrerasatletismo.data.local.PaisEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) de la entidad CARRERA.
 * - Lecturas que observa la UI → devuelven Flow (patrón Observer: Room emite de nuevo al cambiar la tabla).
 * - Escrituras → suspend fun (se ejecutan en una corrutina, sin bloquear la pantalla).
 *
 * SOLID – I (Segregación de interfaces): en vez de un DAO gigante hay uno por entidad
 * (CarreraDao, CorredorDao, CategoriaDao, InscripcionDao); cada quien usa solo lo que necesita.
 */
@Dao
interface CarreraDao {

    // ---------- SELECT ----------
    @Query(
        """SELECT * FROM carrera_vista
           WHERE nombre LIKE '%' || :texto || '%'
              OR ciudad LIKE '%' || :texto || '%'
              OR pais   LIKE '%' || :texto || '%'
           ORDER BY fecha"""
    )
    fun buscar(texto: String): Flow<List<CarreraVista>>

    @Query("SELECT * FROM carrera_vista WHERE idCarrera = :id")
    fun observarPorId(id: String): Flow<CarreraVista?>

    @Query("SELECT * FROM carrera_vista WHERE idCarrera = :id")
    suspend fun obtenerVistaPorId(id: String): CarreraVista?

    @Query("SELECT * FROM carrera WHERE idCarrera = :id")
    suspend fun obtenerPorId(id: String): CarreraEntity?

    @Query("SELECT COUNT(*) FROM carrera")
    suspend fun contar(): Int

    @Query(
        """SELECT COUNT(*) FROM carrera
           WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(:nombre)) AND fecha = :fecha AND idCarrera <> :excluirId"""
    )
    suspend fun contarDuplicadas(nombre: String, fecha: String, excluirId: String): Int

    // ---------- INSERT ----------
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(carrera: CarreraEntity)

    /** Para la API: si la carrera ya existe no la duplica. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarSiNoExiste(carrera: CarreraEntity)

    // ---------- UPDATE ----------
    @Update
    suspend fun actualizar(carrera: CarreraEntity)

    @Query(
        """UPDATE carrera SET tipoRecorrido = :tipo, desnivelM = :desnivel, terreno = :terreno,
           temperaturaC = :temperatura, humedad = :humedad, condiciones = :condiciones
           WHERE idCarrera = :id"""
    )
    suspend fun actualizarDetalle(
        id: String, tipo: String?, desnivel: Int?, terreno: String?,
        temperatura: Double?, humedad: Int?, condiciones: String?
    )

    // ---------- DELETE ----------
    @Delete
    suspend fun eliminar(carrera: CarreraEntity)

    // ---------- País y ciudad (tablas de apoyo) ----------
    @Query("SELECT idPais FROM pais WHERE nombre = :nombre")
    suspend fun idPais(nombre: String): Long?

    @Insert
    suspend fun insertarPais(pais: PaisEntity): Long

    @Query("SELECT idCiudad FROM ciudad WHERE nombre = :nombre AND idPais IS :idPais")
    suspend fun idCiudad(nombre: String, idPais: Long?): Long?

    @Insert
    suspend fun insertarCiudad(ciudad: CiudadEntity): Long
}
