package com.julio.carrerasatletismo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.julio.carrerasatletismo.R
import com.julio.carrerasatletismo.ui.theme.CarrerasAtletismoTheme
import com.julio.carrerasatletismo.ui.viewmodel.CarreraFormUiState
import com.julio.carrerasatletismo.ui.viewmodel.CarreraFormViewModel
import com.julio.carrerasatletismo.ui.viewmodel.MensajeUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun PantallaFormCarrera(
    onRegresar: () -> Unit,
    onGuardado: (Int) -> Unit,
    viewModel: CarreraFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Si se guardó bien, regresamos y la pantalla anterior muestra el mensaje de éxito
    LaunchedEffect(uiState.guardadoCon) {
        uiState.guardadoCon?.let(onGuardado)
    }

    ContenidoFormCarrera(
        uiState = uiState,
        onNombreChange = viewModel::onNombreChange,
        onFechaChange = viewModel::onFechaChange,
        onCiudadChange = viewModel::onCiudadChange,
        onPaisChange = viewModel::onPaisChange,
        onUrlChange = viewModel::onUrlChange,
        onGuardar = viewModel::onGuardar,
        onMensajeMostrado = viewModel::onMensajeMostrado,
        onRegresar = onRegresar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContenidoFormCarrera(
    uiState: CarreraFormUiState,
    onNombreChange: (String) -> Unit = {},
    onFechaChange: (String) -> Unit = {},
    onCiudadChange: (String) -> Unit = {},
    onPaisChange: (String) -> Unit = {},
    onUrlChange: (String) -> Unit = {},
    onGuardar: () -> Unit = {},
    onMensajeMostrado: () -> Unit = {},
    onRegresar: () -> Unit = {}
) {
    val snackbar = remember { SnackbarHostState() }
    EfectoSnackbar(uiState.mensaje, snackbar, onMensajeMostrado)

    // rememberSaveable: si el calendario está abierto y se gira la pantalla, sigue abierto
    var mostrarCalendario by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(if (uiState.esEdicion) R.string.editar_carrera else R.string.agregar_carrera),
                onRegresar = onRegresar
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (uiState.cargando) {
            EstadoCargando(Modifier.padding(padding))
        } else Column(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()                          // el teclado no tapa los campos
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // en horizontal se puede desplazar
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val anchoCampos = Modifier.widthIn(max = 600.dp)
            CampoTexto(
                valor = uiState.nombre,
                onValorChange = onNombreChange,
                etiqueta = stringResource(R.string.nombre_carrera),
                error = uiState.errorNombre,
                ayuda = stringResource(R.string.ayuda_longitud, 3, 80),
                modifier = anchoCampos
            )
            CampoTexto(
                valor = uiState.fecha,
                onValorChange = onFechaChange,
                etiqueta = stringResource(R.string.fecha),
                error = uiState.errorFecha,
                ayuda = stringResource(R.string.ayuda_fecha),
                teclado = KeyboardType.Phone,
                modifier = anchoCampos,
                iconoFinal = {
                    IconButton(onClick = { mostrarCalendario = true }) {
                        Icon(Icons.Filled.DateRange, contentDescription = stringResource(R.string.elegir_fecha))
                    }
                }
            )
            CampoTexto(
                valor = uiState.ciudad,
                onValorChange = onCiudadChange,
                etiqueta = stringResource(R.string.ciudad),
                error = uiState.errorCiudad,
                modifier = anchoCampos
            )
            CampoTexto(
                valor = uiState.pais,
                onValorChange = onPaisChange,
                etiqueta = stringResource(R.string.pais),
                error = uiState.errorPais,
                modifier = anchoCampos
            )
            CampoTexto(
                valor = uiState.url,
                onValorChange = onUrlChange,
                etiqueta = stringResource(R.string.url_opcional),
                error = uiState.errorUrl,
                teclado = KeyboardType.Uri,
                modifier = anchoCampos
            )
            Button(
                onClick = onGuardar,
                enabled = !uiState.guardando,
                modifier = anchoCampos.fillMaxWidth().padding(top = 8.dp)
            ) {
                if (uiState.guardando) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(stringResource(R.string.guardar))
            }
        }
    }

    if (mostrarCalendario) {
        // rememberDatePickerState se guarda solo (usa rememberSaveable por dentro)
        val estadoFecha = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoFecha.selectedDateMillis?.let { onFechaChange(milisATexto(it)) }
                    mostrarCalendario = false
                }) { Text(stringResource(R.string.aceptar)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) { Text(stringResource(R.string.cancelar)) }
            }
        ) {
            DatePicker(state = estadoFecha)
        }
    }
}

/** El DatePicker entrega milisegundos en UTC; los convertimos a "aaaa-mm-dd". */
private fun milisATexto(milis: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(milis))

@Preview(showBackground = true, name = "Formulario con errores")
@Composable
fun PantallaFormCarreraPreview() {
    CarrerasAtletismoTheme {
        ContenidoFormCarrera(
            uiState = CarreraFormUiState(
                nombre = "Ma",
                fecha = "2027-08-29",
                ciudad = "Ciudad de México",
                pais = "México",
                errorNombre = MensajeUi(R.string.error_min_caracteres, listOf(3))
            )
        )
    }
}
