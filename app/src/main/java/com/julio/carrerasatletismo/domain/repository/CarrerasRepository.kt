package com.julio.carrerasatletismo.domain.repository

import com.julio.carrerasatletismo.domain.model.Carrera
import com.julio.carrerasatletismo.domain.model.Categoria
import com.julio.carrerasatletismo.domain.model.Corredor
import com.julio.carrerasatletismo.domain.model.CorredorConCarreras
import com.julio.carrerasatletismo.domain.model.InscritoEnCarrera
import kotlinx.coroutines.flow.Flow

/**
 * PATRÓN REPOSITORY – contrato (interfaz) en la capa domain.
 * Los ViewModels dependen SOLO de esta interfaz; no saben si los datos vienen de Room o de la API.
 * La implementación real está en data/repository/CarrerasRepositoryImpl.kt y Hilt la conecta con @Binds.
 * (SOLID: principio de Inversión de Dependencias – D).
 */
interface CarrerasRepository {

    // ---------- Carreras ----------
    fun observarCarreras(busqueda: String): Flow<List<Carrera>>
    fun observarCarrera(id: String): Flow<Carrera?>
    suspend fun obtenerCarrera(id: String): Carrera?
    suspend fun agregarCarrera(carrera: Carrera): String
    suspend fun actualizarCarrera(carrera: Carrera)
    suspend fun eliminarCarrera(id: String)
    suspend fun hayCarreras(): Boolean
    suspend fun sincronizarCarreras(): Int
    suspend fun cargarDetalleRemoto(id: String)

    // ---------- Corredores ----------
    fun observarCorredores(): Flow<List<Corredor>>
    fun observarCorredoresConCarreras(): Flow<List<CorredorConCarreras>>
    suspend fun obtenerCorredor(id: Long): Corredor?
    suspend fun agregarCorredor(nombre: String, edad: Int)
    suspend fun actualizarCorredor(id: Long, nombre: String, edad: Int)
    suspend fun eliminarCorredor(id: Long)

    // ---------- Categorías ----------
    fun observarCategorias(): Flow<List<Categoria>>
    suspend fun obtenerCategoria(id: Long): Categoria?
    suspend fun agregarCategoria(nombre: String, edadMin: Int, edadMax: Int)
    suspend fun actualizarCategoria(categoria: Categoria)
    suspend fun eliminarCategoria(id: Long)

    // ---------- Inscripciones (N:M) y resultados ----------
    fun observarInscritos(idCarrera: String): Flow<List<InscritoEnCarrera>>
    suspend fun inscribir(idCorredor: Long, idCarrera: String, dorsal: Int, tiempoObjetivo: String)
    suspend fun actualizarInscripcion(idCorredor: Long, idCarrera: String, dorsal: Int, tiempoObjetivo: String)
    suspend fun quitarInscripcion(idCorredor: Long, idCarrera: String)
    suspend fun guardarResultado(idCorredor: Long, idCarrera: String, tiempoReal: String)
    suspend fun borrarResultado(idCorredor: Long, idCarrera: String)
}
