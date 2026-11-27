package com.julio.carrerasatletismo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.julio.carrerasatletismo.data.local.CategoriaConCorredores
import com.julio.carrerasatletismo.data.local.CategoriaEntity
import kotlinx.coroutines.flow.Flow

/** DAO de la entidad CATEGORIA: insert, select, update y delete. */
@Dao
interface CategoriaDao {

    // ---------- SELECT ----------
    @Query("SELECT * FROM categoria ORDER BY edadMin")
    fun observarTodas(): Flow<List<CategoriaEntity>>

    /** Relación 1:N: cada categoría con la lista de sus corredores. */
    @Transaction
    @Query("SELECT * FROM categoria ORDER BY edadMin")
    fun observarConCorredores(): Flow<List<CategoriaConCorredores>>

    @Query("SELECT * FROM categoria WHERE idCategoria = :id")
    suspend fun obtenerPorId(id: Long): CategoriaEntity?

    @Query(
        """SELECT idCategoria FROM categoria
           WHERE :edad BETWEEN edadMin AND edadMax
           ORDER BY edadMin LIMIT 1"""
    )
    suspend fun idParaEdad(edad: Int): Long?

    // ---------- INSERT ----------
    /** ABORT: si el nombre ya existe (índice UNIQUE) lanza SQLiteConstraintException. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(categoria: CategoriaEntity): Long

    // ---------- UPDATE ----------
    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun actualizar(categoria: CategoriaEntity)

    // ---------- DELETE ----------
    @Delete
    suspend fun eliminar(categoria: CategoriaEntity)
}
