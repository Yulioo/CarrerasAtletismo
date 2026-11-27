package com.julio.carrerasatletismo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.julio.carrerasatletismo.data.local.InscripcionEntity
import com.julio.carrerasatletismo.data.local.InscritoEnCarrera
import com.julio.carrerasatletismo.data.local.ResultadoEntity
import kotlinx.coroutines.flow.Flow

/** DAO de la entidad INSCRIPCION (tabla intermedia N:M) y de RESULTADO. */
@Dao
interface InscripcionDao {

    // ---------- SELECT ----------
    /** Inscritos de una carrera con su posición (calculada con una subconsulta). */
    @Query(
        """SELECT c.idCorredor AS idCorredor, c.nombre AS nombre, c.categoria AS categoria,
                  i.dorsal AS dorsal, i.tiempoObjetivo AS tiempoObjetivo,
                  r.tiempoReal AS tiempoReal,
                  CASE WHEN r.tiempoReal IS NULL THEN NULL
                       ELSE (SELECT COUNT(*) + 1 FROM resultado r2
                             WHERE r2.idCarrera = i.idCarrera AND r2.tiempoReal < r.tiempoReal)
                  END AS posicion
           FROM inscripcion i
           INNER JOIN corredor_vista c ON c.idCorredor = i.idCorredor
           LEFT JOIN resultado r ON r.idCorredor = i.idCorredor AND r.idCarrera = i.idCarrera
           WHERE i.idCarrera = :idCarrera
           ORDER BY (r.tiempoReal IS NULL), r.tiempoReal, i.dorsal"""
    )
    fun observarInscritos(idCarrera: String): Flow<List<InscritoEnCarrera>>

    @Query("SELECT * FROM inscripcion WHERE idCorredor = :idCorredor AND idCarrera = :idCarrera")
    suspend fun obtener(idCorredor: Long, idCarrera: String): InscripcionEntity?

    @Query("SELECT idCorredor FROM inscripcion WHERE idCarrera = :idCarrera AND dorsal = :dorsal")
    suspend fun duenoDelDorsal(idCarrera: String, dorsal: Int): Long?

    // ---------- INSERT ----------
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(inscripcion: InscripcionEntity)

    // ---------- UPDATE ----------
    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun actualizar(inscripcion: InscripcionEntity)

    // ---------- DELETE ----------
    @Delete
    suspend fun eliminar(inscripcion: InscripcionEntity)

    // ---------- Resultados ----------
    /** Upsert = inserta si no existe o actualiza si ya existe. */
    @Upsert
    suspend fun guardarResultado(resultado: ResultadoEntity)

    @Delete
    suspend fun eliminarResultado(resultado: ResultadoEntity)
}
