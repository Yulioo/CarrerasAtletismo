package com.julio.carrerasatletismo.ui.navigation

import android.net.Uri

/** Todas las rutas (destinos) del NavHost en un solo lugar. */
object Rutas {
    const val ARG_ID = "id"
    const val SIN_ID = -1L

    /** Clave para pasar el mensaje de éxito de un formulario a la pantalla anterior. */
    const val CLAVE_MENSAJE = "mensaje_exito"

    const val INICIO = "inicio"

    const val CARRERAS = "carreras"
    const val DETALLE_CARRERA = "detalle/{$ARG_ID}"                    // argumento obligatorio
    const val FORM_CARRERA = "formulario_carrera?$ARG_ID={$ARG_ID}"    // argumento opcional

    const val CORREDORES = "corredores"
    const val FORM_CORREDOR = "formulario_corredor?$ARG_ID={$ARG_ID}"  // defaultValue = -1

    const val CATEGORIAS = "categorias"
    const val FORM_CATEGORIA = "formulario_categoria?$ARG_ID={$ARG_ID}" // defaultValue = -1

    // Funciones que arman la ruta real con el valor del argumento
    fun detalleCarrera(id: String) = "detalle/${Uri.encode(id)}"
    fun formCarrera(id: String? = null) =
        if (id == null) "formulario_carrera" else "formulario_carrera?$ARG_ID=${Uri.encode(id)}"
    fun formCorredor(id: Long = SIN_ID) = "formulario_corredor?$ARG_ID=$id"
    fun formCategoria(id: Long = SIN_ID) = "formulario_categoria?$ARG_ID=$id"
}
