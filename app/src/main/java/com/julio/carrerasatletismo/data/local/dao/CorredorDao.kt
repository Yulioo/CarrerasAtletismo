package com.julio.carrerasatletismo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.julio.carrerasatletismo.data.local.CorredorConCarreras
import com.julio.carrerasatletismo.data.local.CorredorEntity
import com.julio.carrerasatletismo.data.local.CorredorVista
import kotlinx.coroutines.flow.Flow

/** DAO de la entidad CORREDOR: insert, select, update y delete. */
@Dao
interface CorredorDao {

    // ---------- SELECT ----------
    @Query("SELECT * FROM corredor_vista ORDER BY nombre")
    fun observarTodos(): Flow<List<CorredorVista>>

    /** @Transaction porque Room hace 2 consultas (corredores + sus carreras) y deben ser consistentes. */
    @Transaction
    @Query("SELECT * FROM corredor_vista ORDER BY nombre")
    fun observarConCarreras(): Flow<List<CorredorConCarreras>>

    @Query("SELECT * FROM corredor WHERE idCorredor = :id")
    suspend fun obtenerPorId(id: Long): CorredorEntity?

    @Query("SELECT * FROM corredor_vista WHERE idCorredor = :id")
    suspend fun obtenerVistaPorId(id: Long): CorredorVista?

    @Query(
        """SELECT COUNT(*) FROM corredor
           WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(:nombre)) AND idCorredor <> :excluirId"""
    )
    suspend fun contarPorNombre(nombre: String, excluirId: Long): Int

    // ---------- INSERT ----------
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(corredor: CorredorEntity): Long

    // ---------- UPDATE ----------
    @Update
    suspend fun actualizar(corredor: CorredorEntity)

    // ---------- DELETE ----------
    @Delete
    suspend fun eliminar(corredor: CorredorEntity)
}
