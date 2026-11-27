package com.julio.carrerasatletismo.domain.model

/**
 * Excepciones PROPIAS de la app. El repositorio convierte los errores técnicos
 * (SQLiteConstraintException, IOException…) en estas excepciones con significado claro,
 * y el ViewModel las traduce a un mensaje personalizado de strings.xml.
 */
sealed class ErrorDominio(mensaje: String) : Exception(mensaje) {
    class NombreDuplicado(val nombre: String) : ErrorDominio("Ya existe un registro llamado $nombre")
    class DorsalOcupado(val dorsal: Int) : ErrorDominio("El dorsal $dorsal ya está ocupado")
    class YaInscrito : ErrorDominio("El corredor ya está inscrito en la carrera")
    class CategoriaEnUso(val nombre: String) : ErrorDominio("La categoría $nombre tiene corredores")
    class SinCategoriaParaEdad(val edad: Int) : ErrorDominio("No hay categoría para $edad años")
    class NoEncontrado : ErrorDominio("El registro no existe")
    class SinConexion : ErrorDominio("Sin conexión a internet")
}
