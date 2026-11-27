package com.julio.carrerasatletismo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase Application: es lo PRIMERO que Android crea al abrir la app.
 * @HiltAndroidApp genera el contenedor de dependencias de Hilt para toda la aplicación.
 * Se registra en AndroidManifest.xml con android:name=".CarrerasApp".
 */
@HiltAndroidApp
class CarrerasApp : Application()
