package com.julio.carrerasatletismo.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/* ------------------------------------------------------------------
 * ENTIDADES DE ROOM (tablas de SQLite) – versión 2 de la base de datos
 *
 * Las 4 entidades principales con CRUD completo son:
 *   1. CarreraEntity     (tabla carrera)
 *   2. CorredorEntity    (tabla corredor)
 *   3. CategoriaEntity   (tabla categoria)
 *   4. InscripcionEntity (tabla inscripcion)  ← tabla intermedia N:M
 *
 * Relaciones:
 *   - CATEGORIA 1 ── N CORREDOR            (relación 1:N)
 *   - CORREDOR  N ── M CARRERA vía INSCRIPCION (relación N:M)
 *
 * Tablas de apoyo (normalización): PAIS, CIUDAD y RESULTADO.
 * ------------------------------------------------------------------ */

@Entity(
    tableName = "pais",
    indices = [Index(value = ["nombre"], unique = true)] // no se repiten países
)
data class PaisEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "idPais") val idPais: Long = 0,
    @ColumnInfo(name = "nombre") val nombre: String
)

@Entity(
    tableName = "ciudad",
    foreignKeys = [
        ForeignKey(
            entity = PaisEntity::class,
            parentColumns = ["idPais"],
            childColumns = ["idPais"],
            onDelete = ForeignKey.SET_NULL // si se borra el país, la ciudad queda sin país
        )
    ],
    indices = [Index("idPais"), Index(value = ["nombre", "idPais"], unique = true)]
)
data class CiudadEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "idCiudad") val idCiudad: Long = 0,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "idPais") val idPais: Long?
)

/** Entidad 3: CATEGORIA (Juvenil, Libre, Máster…). Lado "1" de la relación 1:N con corredor. */
@Entity(
    tableName = "categoria",
    indices = [Index(value = ["nombre"], unique = true)] // UNIQUE: no puede haber dos categorías con el mismo nombre
)
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "idCategoria") val idCategoria: Long = 0,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "edadMin") val edadMin: Int,
    @ColumnInfo(name = "edadMax") val edadMax: Int
)

/** Entidad 1: CARRERA (maratón). */
@Entity(
    tableName = "carrera",
    foreignKeys = [
        ForeignKey(
            entity = CiudadEntity::class,
            parentColumns = ["idCiudad"],
            childColumns = ["idCiudad"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("idCiudad")]
)
data class CarreraEntity(
    @PrimaryKey @ColumnInfo(name = "idCarrera") val idCarrera: String,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "fecha") val fecha: String,
    @ColumnInfo(name = "idCiudad") val idCiudad: Long?,
    @ColumnInfo(name = "url") val url: String,

    @ColumnInfo(name = "tipoRecorrido") val tipoRecorrido: String? = null,
    @ColumnInfo(name = "desnivelM") val desnivelM: Int? = null,
    @ColumnInfo(name = "terreno") val terreno: String? = null,
    @ColumnInfo(name = "temperaturaC") val temperaturaC: Double? = null,
    @ColumnInfo(name = "humedad") val humedad: Int? = null,
    @ColumnInfo(name = "condiciones") val condiciones: String? = null
)

/** Entidad 2: CORREDOR. Lado "N" de la relación 1:N con categoría. */
@Entity(
    tableName = "corredor",
    foreignKeys = [
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["idCategoria"],
            childColumns = ["idCategoria"],
            // RESTRICT: no se deja borrar una categoría que todavía tiene corredores.
            // El repositorio atrapa el error y muestra un mensaje claro.
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("idCategoria")]
)
data class CorredorEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "idCorredor") val idCorredor: Long = 0,
    @ColumnInfo(name = "nombre") val nombre: String,
    @ColumnInfo(name = "edad") val edad: Int,
    @ColumnInfo(name = "idCategoria") val idCategoria: Long
)

/** Entidad 4: INSCRIPCION – tabla intermedia de la relación N:M corredor ↔ carrera. */
@Entity(
    tableName = "inscripcion",
    primaryKeys = ["idCorredor", "idCarrera"],
    foreignKeys = [
        ForeignKey(
            entity = CorredorEntity::class,
            parentColumns = ["idCorredor"],
            childColumns = ["idCorredor"],
            onDelete = ForeignKey.CASCADE // si se borra el corredor, se borran sus inscripciones
        ),
        ForeignKey(
            entity = CarreraEntity::class,
            parentColumns = ["idCarrera"],
            childColumns = ["idCarrera"],
            onDelete = ForeignKey.CASCADE // si se borra la carrera, se borran sus inscripciones
        )
    ],
    indices = [
        Index("idCarrera"),
        // UNIQUE: dos corredores no pueden llevar el mismo dorsal en la misma carrera
        Index(value = ["idCarrera", "dorsal"], unique = true)
    ]
)
data class InscripcionEntity(
    @ColumnInfo(name = "idCorredor") val idCorredor: Long,
    @ColumnInfo(name = "idCarrera") val idCarrera: String,
    @ColumnInfo(name = "dorsal") val dorsal: Int,
    @ColumnInfo(name = "tiempoObjetivo") val tiempoObjetivo: String
)

/** Tiempo final de un corredor inscrito. Depende de la inscripción (CASCADE). */
@Entity(
    tableName = "resultado",
    primaryKeys = ["idCorredor", "idCarrera"],
    foreignKeys = [
        ForeignKey(
            entity = InscripcionEntity::class,
            parentColumns = ["idCorredor", "idCarrera"],
            childColumns = ["idCorredor", "idCarrera"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ResultadoEntity(
    @ColumnInfo(name = "idCorredor") val idCorredor: Long,
    @ColumnInfo(name = "idCarrera") val idCarrera: String,
    @ColumnInfo(name = "tiempoReal") val tiempoReal: String
)
