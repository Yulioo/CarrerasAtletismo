package com.julio.carrerasatletismo.data.local

import androidx.sqlite.db.SupportSQLiteDatabase

/*
 * Datos iniciales: se insertan UNA sola vez, cuando la base de datos se crea por primera vez
 * (callback onCreate de AppDatabase). Así la app no arranca vacía y funciona sin internet.
 * Si el usuario ya tiene datos, no se tocan (onCreate no se vuelve a ejecutar).
 */

fun sembrarCategorias(db: SupportSQLiteDatabase) {
    db.execSQL(
        """INSERT OR IGNORE INTO categoria (nombre, edadMin, edadMax) VALUES
           ('Juvenil', 0, 17), ('Libre', 18, 39), ('Máster', 40, 999)"""
    )
}

fun sembrarDatosIniciales(db: SupportSQLiteDatabase) {
    sembrarCategorias(db)

    db.execSQL("INSERT OR IGNORE INTO pais (nombre) VALUES ('México'), ('Estados Unidos'), ('Japón')")

    db.execSQL(
        """INSERT OR IGNORE INTO ciudad (nombre, idPais)
           SELECT 'Ciudad de México', idPais FROM pais WHERE nombre = 'México'"""
    )
    db.execSQL(
        """INSERT OR IGNORE INTO ciudad (nombre, idPais)
           SELECT 'Guadalajara', idPais FROM pais WHERE nombre = 'México'"""
    )
    db.execSQL(
        """INSERT OR IGNORE INTO ciudad (nombre, idPais)
           SELECT 'Boston', idPais FROM pais WHERE nombre = 'Estados Unidos'"""
    )
    db.execSQL(
        """INSERT OR IGNORE INTO ciudad (nombre, idPais)
           SELECT 'Tokio', idPais FROM pais WHERE nombre = 'Japón'"""
    )

    db.execSQL(
        """INSERT OR IGNORE INTO carrera
           (idCarrera, nombre, fecha, idCiudad, url, tipoRecorrido, desnivelM, terreno, temperaturaC, humedad, condiciones)
           VALUES
           ('local-cdmx-2027', 'Maratón de la Ciudad de México', '2027-08-29',
            (SELECT idCiudad FROM ciudad WHERE nombre = 'Ciudad de México'), '',
            'loop', 120, 'road', 14.5, 70, 'Fresco'),
           ('local-gdl-2027', 'Maratón Internacional de Guadalajara', '2027-02-21',
            (SELECT idCiudad FROM ciudad WHERE nombre = 'Guadalajara'), '',
            'loop', 60, 'road', 16.0, 45, 'Soleado'),
           ('local-boston-2027', 'Maratón de Boston', '2027-04-19',
            (SELECT idCiudad FROM ciudad WHERE nombre = 'Boston'), '',
            'point-to-point', 248, 'road', 11.0, 60, 'Templado'),
           ('local-tokio-2027', 'Maratón de Tokio', '2027-03-07',
            (SELECT idCiudad FROM ciudad WHERE nombre = 'Tokio'), '',
            'point-to-point', 40, 'road', 9.0, 55, 'Fresco')"""
    )

    db.execSQL(
        """INSERT INTO corredor (nombre, edad, idCategoria) VALUES
           ('Ana López', 28, (SELECT idCategoria FROM categoria WHERE nombre = 'Libre')),
           ('Luis Pérez', 45, (SELECT idCategoria FROM categoria WHERE nombre = 'Máster')),
           ('Sofía Hernández', 16, (SELECT idCategoria FROM categoria WHERE nombre = 'Juvenil'))"""
    )

    db.execSQL(
        """INSERT OR IGNORE INTO inscripcion (idCorredor, idCarrera, dorsal, tiempoObjetivo)
           SELECT idCorredor, 'local-cdmx-2027', 101, '03:45:00' FROM corredor WHERE nombre = 'Ana López'"""
    )
    db.execSQL(
        """INSERT OR IGNORE INTO inscripcion (idCorredor, idCarrera, dorsal, tiempoObjetivo)
           SELECT idCorredor, 'local-cdmx-2027', 102, '04:10:00' FROM corredor WHERE nombre = 'Luis Pérez'"""
    )
}
