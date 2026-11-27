package com.julio.carrerasatletismo.data.local

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Relación N:M (muchos a muchos): un corredor con todas las carreras en las que está inscrito.
 * @Embedded pone las columnas del corredor; @Relation + Junction usa la tabla intermedia
 * INSCRIPCION para buscar las carreras.
 */
data class CorredorConCarreras(
    @Embedded val corredor: CorredorVista,
    @Relation(
        parentColumn = "idCorredor",
        entityColumn = "idCarrera",
        associateBy = Junction(InscripcionEntity::class)
    )
    val carreras: List<CarreraVista>
)

/**
 * Relación 1:N (uno a muchos): una categoría con todos sus corredores.
 * La columna idCategoria de CORREDOR apunta a la categoría.
 */
data class CategoriaConCorredores(
    @Embedded val categoria: CategoriaEntity,
    @Relation(
        parentColumn = "idCategoria",
        entityColumn = "idCategoria"
    )
    val corredores: List<CorredorEntity>
)

/** Resultado de la consulta de inscritos de una carrera (JOIN de varias tablas). */
data class InscritoEnCarrera(
    val idCorredor: Long,
    val nombre: String,
    val categoria: String,
    val dorsal: Int,
    val tiempoObjetivo: String,
    val tiempoReal: String? = null,
    val posicion: Int? = null
)
