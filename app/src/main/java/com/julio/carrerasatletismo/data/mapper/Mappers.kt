package com.julio.carrerasatletismo.data.mapper

import com.julio.carrerasatletismo.data.local.CarreraEntity
import com.julio.carrerasatletismo.data.local.CarreraVista
import com.julio.carrerasatletismo.data.local.CategoriaConCorredores
import com.julio.carrerasatletismo.data.local.CategoriaEntity
import com.julio.carrerasatletismo.data.local.CorredorVista
import com.julio.carrerasatletismo.data.local.CorredorConCarreras as CorredorConCarrerasLocal
import com.julio.carrerasatletismo.data.local.InscritoEnCarrera as InscritoLocal
import com.julio.carrerasatletismo.domain.model.Carrera
import com.julio.carrerasatletismo.domain.model.Categoria
import com.julio.carrerasatletismo.domain.model.Corredor
import com.julio.carrerasatletismo.domain.model.CorredorConCarreras
import com.julio.carrerasatletismo.domain.model.InscritoEnCarrera

/*
 * MAPPERS = FUNCIONES DE EXTENSIÓN.
 * "fun CarreraVista.toDomain()" agrega un método nuevo a la clase CarreraVista sin modificarla.
 * Convierten objetos de Room (capa data) ↔ modelos de dominio (capa domain).
 */

// ---------- Entity / Vista → Dominio ----------
fun CarreraVista.toDomain() = Carrera(
    idCarrera = idCarrera,
    nombre = nombre,
    fecha = fecha,
    ciudad = ciudad,
    pais = pais,
    url = url,
    tipoRecorrido = tipoRecorrido,
    desnivelM = desnivelM,
    terreno = terreno,
    temperaturaC = temperaturaC,
    humedad = humedad,
    condiciones = condiciones
)

fun CorredorVista.toDomain() = Corredor(
    idCorredor = idCorredor,
    nombre = nombre,
    edad = edad,
    categoria = categoria,
    idCategoria = idCategoria
)

fun CategoriaEntity.toDomain(totalCorredores: Int = 0) = Categoria(
    idCategoria = idCategoria,
    nombre = nombre,
    edadMin = edadMin,
    edadMax = edadMax,
    totalCorredores = totalCorredores
)

fun CategoriaConCorredores.toDomain() = categoria.toDomain(totalCorredores = corredores.size)

fun CorredorConCarrerasLocal.toDomain() = CorredorConCarreras(
    corredor = corredor.toDomain(),
    carreras = carreras.map { it.toDomain() }
)

fun InscritoLocal.toDomain() = InscritoEnCarrera(
    idCorredor = idCorredor,
    nombre = nombre,
    categoria = categoria,
    dorsal = dorsal,
    tiempoObjetivo = tiempoObjetivo,
    tiempoReal = tiempoReal,
    posicion = posicion
)

// ---------- Dominio → Entity ----------
fun Carrera.toEntity(idCiudad: Long?) = CarreraEntity(
    idCarrera = idCarrera,
    nombre = nombre,
    fecha = fecha,
    idCiudad = idCiudad,
    url = url,
    tipoRecorrido = tipoRecorrido,
    desnivelM = desnivelM,
    terreno = terreno,
    temperaturaC = temperaturaC,
    humedad = humedad,
    condiciones = condiciones
)

fun Categoria.toEntity() = CategoriaEntity(
    idCategoria = idCategoria,
    nombre = nombre,
    edadMin = edadMin,
    edadMax = edadMax
)
