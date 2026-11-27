package com.julio.carrerasatletismo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.julio.carrerasatletismo.ui.navigation.NavegacionApp
import com.julio.carrerasatletismo.ui.theme.CarrerasAtletismoTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Punto de entrada de la interfaz (la Activity con el intent-filter LAUNCHER en el Manifest).
 * @AndroidEntryPoint permite que Hilt inyecte dependencias aquí y en los ViewModels
 * que piden las pantallas con hiltViewModel().
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarrerasAtletismoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // El NavHost decide qué pantalla mostrar (no usamos variables "ruta" propias)
                    NavegacionApp()
                }
            }
        }
    }
}
