package com.julio.carrerasatletismo.di

import android.content.Context
import com.julio.carrerasatletismo.data.local.AppDatabase
import com.julio.carrerasatletismo.data.local.dao.CarreraDao
import com.julio.carrerasatletismo.data.local.dao.CategoriaDao
import com.julio.carrerasatletismo.data.local.dao.CorredorDao
import com.julio.carrerasatletismo.data.local.dao.InscripcionDao
import com.julio.carrerasatletismo.data.remote.RetrofitClient
import com.julio.carrerasatletismo.data.remote.RunDidaApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * INYECCIÓN DE DEPENDENCIAS CON HILT – módulo de datos.
 * @Module: aquí le explicamos a Hilt CÓMO crear objetos que no podemos anotar con @Inject
 *          (la base de datos de Room, los DAO y la API de Retrofit).
 * @InstallIn(SingletonComponent::class): estos objetos viven mientras viva la aplicación.
 * @Provides: función que fabrica una dependencia.
 * @Singleton: Hilt crea UNA sola instancia y siempre entrega la misma.
 */
@Module
@InstallIn(SingletonComponent::class)
object ModuloDatos {

    @Provides
    @Singleton
    fun proveerBaseDeDatos(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.obtenerInstancia(context)

    @Provides
    @Singleton
    fun proveerCarreraDao(db: AppDatabase): CarreraDao = db.carreraDao()

    @Provides
    @Singleton
    fun proveerCorredorDao(db: AppDatabase): CorredorDao = db.corredorDao()

    @Provides
    @Singleton
    fun proveerCategoriaDao(db: AppDatabase): CategoriaDao = db.categoriaDao()

    @Provides
    @Singleton
    fun proveerInscripcionDao(db: AppDatabase): InscripcionDao = db.inscripcionDao()

    @Provides
    @Singleton
    fun proveerApi(): RunDidaApi = RetrofitClient.api
}
