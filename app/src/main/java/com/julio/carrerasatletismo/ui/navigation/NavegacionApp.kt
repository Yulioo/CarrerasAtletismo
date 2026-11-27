package com.julio.carrerasatletismo.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.julio.carrerasatletismo.R
import com.julio.carrerasatletismo.ui.screens.PantallaCarreras
import com.julio.carrerasatletismo.ui.screens.PantallaCategorias
import com.julio.carrerasatletismo.ui.screens.PantallaDetalleCarrera
import com.julio.carrerasatletismo.ui.screens.PantallaFormCarrera
import com.julio.carrerasatletismo.ui.screens.PantallaFormCategoria
import com.julio.carrerasatletismo.ui.screens.PantallaFormCorredor
import com.julio.carrerasatletismo.ui.screens.PantallaCorredores
import com.julio.carrerasatletismo.ui.screens.PantallaInicio

/**
 * NAVEGACIÓN con Navigation Compose.
 * - rememberNavController(): crea y recuerda el controlador (también al girar la pantalla).
 * - NavHost: el contenedor que muestra la pantalla de la ruta actual.
 * - composable("ruta") { … }: función de NavGraphBuilder que registra cada pantalla.
 * Cada pantalla obtiene su propio ViewModel con hiltViewModel(), ligado a su entrada de navegación.
 */
@Composable
fun NavegacionApp(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Rutas.INICIO) {

        composable(Rutas.INICIO) {
            PantallaInicio(
                onVerCarreras = { navController.navigate(Rutas.CARRERAS) },
                onVerCorredores = { navController.navigate(Rutas.CORREDORES) },
                onVerCategorias = { navController.navigate(Rutas.CATEGORIAS) }
            )
        }

        // ---------------- Carreras ----------------
        composable(Rutas.CARRERAS) { entrada ->
            val mensajeExito by entrada.mensajeExito()
            PantallaCarreras(
                mensajeExito = mensajeExito,
                onMensajeExitoMostrado = { entrada.limpiarMensajeExito() },
                onRegresar = { navController.regresar() },
                onAbrirCarrera = { id -> navController.navigate(Rutas.detalleCarrera(id)) },
                onAgregar = { navController.navigate(Rutas.formCarrera()) }
            )
        }

        composable(
            route = Rutas.DETALLE_CARRERA,
            arguments = listOf(navArgument(Rutas.ARG_ID) { type = NavType.StringType })
        ) { entrada ->
            val mensajeExito by entrada.mensajeExito()
            PantallaDetalleCarrera(
                mensajeExito = mensajeExito,
                onMensajeExitoMostrado = { entrada.limpiarMensajeExito() },
                onRegresar = { navController.regresar() },
                onEditar = { id -> navController.navigate(Rutas.formCarrera(id)) },
                onEliminada = { navController.regresarConMensaje(R.string.msg_carrera_eliminada) }
            )
        }

        composable(
            route = Rutas.FORM_CARRERA,
            arguments = listOf(navArgument(Rutas.ARG_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            PantallaFormCarrera(
                onRegresar = { navController.regresar() },
                onGuardado = { mensaje -> navController.regresarConMensaje(mensaje) }
            )
        }

        // ---------------- Corredores ----------------
        composable(Rutas.CORREDORES) { entrada ->
            val mensajeExito by entrada.mensajeExito()
            PantallaCorredores(
                mensajeExito = mensajeExito,
                onMensajeExitoMostrado = { entrada.limpiarMensajeExito() },
                onRegresar = { navController.regresar() },
                onAgregar = { navController.navigate(Rutas.formCorredor()) },
                onEditar = { id -> navController.navigate(Rutas.formCorredor(id)) }
            )
        }

        composable(
            route = Rutas.FORM_CORREDOR,
            arguments = listOf(navArgument(Rutas.ARG_ID) {
                type = NavType.LongType
                defaultValue = Rutas.SIN_ID
            })
        ) {
            PantallaFormCorredor(
                onRegresar = { navController.regresar() },
                onGuardado = { mensaje -> navController.regresarConMensaje(mensaje) }
            )
        }

        // ---------------- Categorías ----------------
        composable(Rutas.CATEGORIAS) { entrada ->
            val mensajeExito by entrada.mensajeExito()
            PantallaCategorias(
                mensajeExito = mensajeExito,
                onMensajeExitoMostrado = { entrada.limpiarMensajeExito() },
                onRegresar = { navController.regresar() },
                onAgregar = { navController.navigate(Rutas.formCategoria()) },
                onEditar = { id -> navController.navigate(Rutas.formCategoria(id)) }
            )
        }

        composable(
            route = Rutas.FORM_CATEGORIA,
            arguments = listOf(navArgument(Rutas.ARG_ID) {
                type = NavType.LongType
                defaultValue = Rutas.SIN_ID
            })
        ) {
            PantallaFormCategoria(
                onRegresar = { navController.regresar() },
                onGuardado = { mensaje -> navController.regresarConMensaje(mensaje) }
            )
        }
    }
}

/** Mensaje de éxito que dejó un formulario en esta entrada (sobrevive a la rotación). */
@Composable
private fun NavBackStackEntry.mensajeExito() =
    savedStateHandle.getStateFlow<Int?>(Rutas.CLAVE_MENSAJE, null).collectAsState()

private fun NavBackStackEntry.limpiarMensajeExito() {
    savedStateHandle[Rutas.CLAVE_MENSAJE] = null
}

/**
 * Regresa solo si hay una pantalla anterior y la actual está activa (RESUMED).
 * Así un doble toque en "regresar" no saca dos pantallas ni deja la app en blanco.
 */
private fun NavHostController.regresar() {
    val actualActiva = currentBackStackEntry?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) == true
    if (actualActiva && previousBackStackEntry != null) popBackStack()
}

/** Deja el mensaje de éxito en la pantalla anterior y regresa a ella. */
private fun NavHostController.regresarConMensaje(@StringRes mensaje: Int) {
    previousBackStackEntry?.savedStateHandle?.set(Rutas.CLAVE_MENSAJE, mensaje)
    regresar()
}
