package com.julio.carrerasatletismo.ui.viewmodel

import android.util.Patterns
import com.julio.carrerasatletismo.R
import com.julio.carrerasatletismo.domain.normalizarTiempo
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Reglas de validación de los formularios. Cada función regresa null si el valor es correcto
 * o un MensajeUi con el error personalizado (obligatorio, longitud, formato numérico…).
 * (SOLID – S: esta clase solo tiene la responsabilidad de validar.)
 */
object Validaciones {

    private val SOLO_LETRAS = Regex("""^[\p{L} .'-]+$""")

    fun texto(valor: String, min: Int, max: Int): MensajeUi? {
        val t = valor.trim()
        return when {
            t.isEmpty() -> MensajeUi(R.string.error_obligatorio)
            t.length < min -> MensajeUi(R.string.error_min_caracteres, listOf(min))
            t.length > max -> MensajeUi(R.string.error_max_caracteres, listOf(max))
            else -> null
        }
    }

    fun nombrePersona(valor: String): MensajeUi? =
        texto(valor, 3, 60) ?: if (!SOLO_LETRAS.matches(valor.trim())) MensajeUi(R.string.error_solo_letras) else null

    fun entero(valor: String, min: Int, max: Int): MensajeUi? {
        val t = valor.trim()
        if (t.isEmpty()) return MensajeUi(R.string.error_obligatorio)
        val numero = t.toIntOrNull() ?: return MensajeUi(R.string.error_numero)
        if (numero < min || numero > max) return MensajeUi(R.string.error_rango, listOf(min, max))
        return null
    }

    fun fecha(valor: String): MensajeUi? {
        val t = valor.trim()
        if (t.isEmpty()) return MensajeUi(R.string.error_obligatorio)
        if (!Regex("""^\d{4}-\d{2}-\d{2}$""").matches(t)) return MensajeUi(R.string.error_fecha)
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(t)
            null
        } catch (e: ParseException) {
            MensajeUi(R.string.error_fecha)
        }
    }

    fun url(valor: String): MensajeUi? {
        val t = valor.trim()
        if (t.isEmpty()) return null // la URL es opcional
        val valida = (t.startsWith("http://") || t.startsWith("https://")) && Patterns.WEB_URL.matcher(t).matches()
        return if (valida) null else MensajeUi(R.string.error_url)
    }

    fun tiempo(valor: String): MensajeUi? = when {
        valor.isBlank() -> MensajeUi(R.string.error_obligatorio)
        normalizarTiempo(valor) == null -> MensajeUi(R.string.error_tiempo)
        else -> null
    }
}
