package com.julio.carrerasatletismo.domain

/**
 * Acepta "h:mm:ss", "hh:mm:ss" o solo números "hmmss"/"hhmmss" (ej. 34500 = 3:45:00)
 * y lo deja como hh:mm:ss para poder ordenar los tiempos como texto.
 * Regresa null si el tiempo no es válido.
 */
fun normalizarTiempo(texto: String): String? {
    val limpio = texto.trim()
    val conPuntos = Regex("""^(\d{1,2}):([0-5]\d):([0-5]\d)$""").find(limpio)
    val soloNumeros = Regex("""^(\d{1,2})([0-5]\d)([0-5]\d)$""").find(limpio)
    val m = conPuntos ?: soloNumeros ?: return null
    val (h, min, s) = m.destructured
    if (h.toInt() == 0 && min.toInt() == 0 && s.toInt() == 0) return null
    return "%02d:%s:%s".format(h.toInt(), min, s)
}
