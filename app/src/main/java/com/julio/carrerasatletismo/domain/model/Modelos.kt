package com.julio.carrerasatletismo.domain.model

// MODELOS DE DOMINIO: clases puras de Kotlin, sin anotaciones de Room ni de Retrofit.
// La interfaz (UI) y los ViewModels solo conocen estos modelos.

data class Carrera(
    val idCarrera: String,
    val nombre: String,
    val fecha: String,      // formato aaaa-mm-dd
    val ciudad: String,
    val pais: String,
    val url: String,
    val tipoRecorrido: String? = null,
    val desnivelM: Int? = null,
    val terreno: String? = null,
    val temperaturaC: Double? = null,
    val humedad: Int? = null,
    val condiciones: String? = null
) {
    /** Las carreras creadas en el celular empiezan con "local-"; las demás vienen de la API. */
    val esLocal: Boolean get() = idCarrera.startsWith(PREFIJO_LOCAL)

    companion object {
        const val PREFIJO_LOCAL = "local-"
    }
}

data class Corredor(
    val idCorredor: Long,
    val nombre: String,
    val edad: Int,
    val categoria: String,
    val idCategoria: Long = 0
)

data class Categoria(
    val idCategoria: Long,
    val nombre: String,
    val edadMin: Int,
    val edadMax: Int,
    val totalCorredores: Int = 0
)

data class CorredorConCarreras(
    val corredor: Corredor,
    val carreras: List<Carrera>
)

data class InscritoEnCarrera(
    val idCorredor: Long,
    val nombre: String,
    val categoria: String,
    val dorsal: Int,
    val tiempoObjetivo: String,
    val tiempoReal: String? = null,
    val posicion: Int? = null
)
