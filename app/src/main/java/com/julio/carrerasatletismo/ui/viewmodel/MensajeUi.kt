package com.julio.carrerasatletismo.ui.viewmodel

import androidx.annotation.StringRes
import com.julio.carrerasatletismo.R
import com.julio.carrerasatletismo.domain.model.ErrorDominio

/**
 * Un mensaje para el usuario: el id del texto en strings.xml + sus argumentos (%1$s, %1$d…).
 * El ViewModel no guarda textos ya traducidos, solo la referencia; la pantalla lo convierte
 * con stringResource(). Así TODOS los mensajes viven en strings.xml.
 */
data class MensajeUi(
    @StringRes val idTexto: Int,
    val argumentos: List<Any> = emptyList()
)

/** Estado genérico de una lista que viene de la base de datos. */
sealed interface EstadoLista<out T> {
    data object Cargando : EstadoLista<Nothing>
    data class Listo<T>(val datos: List<T>) : EstadoLista<T>
    data class Error(val mensaje: MensajeUi) : EstadoLista<Nothing>
}

/** Función de extensión: convierte cualquier error en un mensaje personalizado de strings.xml. */
fun Throwable.aMensajeUi(): MensajeUi = when (this) {
    is ErrorDominio.NombreDuplicado -> MensajeUi(R.string.error_nombre_duplicado, listOf(nombre))
    is ErrorDominio.DorsalOcupado -> MensajeUi(R.string.error_dorsal_ocupado, listOf(dorsal))
    is ErrorDominio.YaInscrito -> MensajeUi(R.string.error_ya_inscrito)
    is ErrorDominio.CategoriaEnUso -> MensajeUi(R.string.error_categoria_en_uso, listOf(nombre))
    is ErrorDominio.SinCategoriaParaEdad -> MensajeUi(R.string.error_sin_categoria, listOf(edad))
    is ErrorDominio.NoEncontrado -> MensajeUi(R.string.error_no_encontrado)
    is ErrorDominio.SinConexion -> MensajeUi(R.string.error_sin_conexion)
    else -> MensajeUi(R.string.error_inesperado)
}
