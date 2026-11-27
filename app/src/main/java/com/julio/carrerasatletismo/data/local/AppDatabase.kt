package com.julio.carrerasatletismo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.julio.carrerasatletismo.data.local.dao.CarreraDao
import com.julio.carrerasatletismo.data.local.dao.CategoriaDao
import com.julio.carrerasatletismo.data.local.dao.CorredorDao
import com.julio.carrerasatletismo.data.local.dao.InscripcionDao

/**
 * @Database: la base de datos SQLite de la app (archivo carreras.db).
 * Lista todas las entidades (tablas) y vistas. La versión 2 tiene la migración MIGRACION_1_2.
 */
@Database(
    entities = [
        PaisEntity::class,
        CiudadEntity::class,
        CategoriaEntity::class,
        CarreraEntity::class,
        CorredorEntity::class,
        InscripcionEntity::class,
        ResultadoEntity::class
    ],
    views = [CarreraVista::class, CorredorVista::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun carreraDao(): CarreraDao
    abstract fun corredorDao(): CorredorDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun inscripcionDao(): InscripcionDao

    // PATRÓN SINGLETON: solo puede existir UNA instancia de la base de datos en toda la app.
    companion object {
        private const val NOMBRE_ARCHIVO = "carreras.db"

        // @Volatile: todos los hilos ven siempre el valor más reciente de INSTANCIA.
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        fun obtenerInstancia(context: Context): AppDatabase =
            // 1) Si ya existe, se regresa la misma.
            INSTANCIA ?: synchronized(this) {
                // 2) synchronized: solo un hilo a la vez entra aquí, así dos hilos
                //    no pueden crear dos bases de datos al mismo tiempo.
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    NOMBRE_ARCHIVO
                )
                    .addMigrations(MIGRACION_1_2) // conserva los datos del usuario al actualizar
                    .addCallback(object : Callback() {
                        // Solo se ejecuta la PRIMERA vez que se crea la base de datos
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            sembrarDatosIniciales(db)
                        }
                    })
                    .build()
                    .also { INSTANCIA = it }
            }
    }
}
