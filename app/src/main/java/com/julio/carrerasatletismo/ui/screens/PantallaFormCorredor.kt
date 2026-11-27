package com.julio.carrerasatletismo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.julio.carrerasatletismo.R
import com.julio.carrerasatletismo.ui.theme.CarrerasAtletismoTheme
import com.julio.carrerasatletismo.ui.viewmodel.CorredorFormUiState
import com.julio.carrerasatletismo.ui.viewmodel.CorredorFormViewModel

@Composable
fun PantallaFormCorredor(
    onRegresar: () -> Unit,
    onGuardado: (Int) -> Unit,
    viewModel: CorredorFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.guardadoCon) {
        uiState.guardadoCon?.let(onGuardado)
    }
    ContenidoFormCorredor(
        uiState = uiState,
        onNombreChange = viewModel::onNombreChange,
        onEdadChange = viewModel::onEdadChange,
        onGuardar = viewModel::onGuardar,
        onMensajeMostrado = viewModel::onMensajeMostrado,
        onRegresar = onRegresar
    )
}

@Composable
fun ContenidoFormCorredor(
    uiState: CorredorFormUiState,
    onNombreChange: (String) -> Unit = {},
    onEdadChange: (String) -> Unit = {},
    onGuardar: () -> Unit = {},
    onMensajeMostrado: () -> Unit = {},
    onRegresar: () -> Unit = {}
) {
    val snackbar = remember { SnackbarHostState() }
    EfectoSnackbar(uiState.mensaje, snackbar, onMensajeMostrado)

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(if (uiState.esEdicion) R.string.editar_corredor else R.string.agregar_corredor),
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
                .imePadding()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val ancho = Modifier.widthIn(max = 600.dp)
            CampoTexto(
                valor = uiState.nombre,
                onValorChange = onNombreChange,
                etiqueta = stringResource(R.string.nombre),
                error = uiState.errorNombre,
                ayuda = stringResource(R.string.ayuda_longitud, 3, 60),
                modifier = ancho
            )
            CampoTexto(
                valor = uiState.edad,
                onValorChange = onEdadChange,
                etiqueta = stringResource(R.string.edad),
                error = uiState.errorEdad,
                ayuda = stringResource(R.string.ayuda_edad),
                teclado = KeyboardType.Number,
                modifier = ancho
            )
            Text(
                text = uiState.categoriaSugerida?.let { stringResource(R.string.categoria_asignada, it) }
                    ?: stringResource(R.string.categoria_automatica),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = ancho
            )
            BotonGuardar(uiState.guardando, onGuardar, ancho)
        }
    }
}

@Preview(showBackground = true, name = "Formulario corredor")
@Composable
fun PantallaFormCorredorPreview() {
    CarrerasAtletismoTheme {
        ContenidoFormCorredor(uiState = CorredorFormUiState(nombre = "Ana López", edad = "28", categoriaSugerida = "Libre"))
    }
}
