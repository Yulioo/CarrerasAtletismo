package com.julio.carrerasatletismo.di

import com.julio.carrerasatletismo.data.repository.CarrerasRepositoryImpl
import com.julio.carrerasatletismo.domain.repository.CarrerasRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @Binds vincula la INTERFAZ con su IMPLEMENTACIÓN:
 * cuando un ViewModel pide un CarrerasRepository, Hilt le entrega un CarrerasRepositoryImpl.
 * Así el ViewModel nunca conoce la clase concreta (patrón Repository + principio D de SOLID).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ModuloRepositorio {

    @Binds
    @Singleton
    abstract fun vincularRepositorio(impl: CarrerasRepositoryImpl): CarrerasRepository
}
