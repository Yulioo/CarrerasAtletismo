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
import com.julio.carrerasatletismo.ui.viewmodel.CategoriaFormUiState
import com.julio.carrerasatletismo.ui.viewmodel.CategoriaFormViewModel
import com.julio.carrerasatletismo.ui.viewmodel.MensajeUi

@Composable
fun PantallaFormCategoria(
    onRegresar: () -> Unit,
    onGuardado: (Int) -> Unit,
    viewModel: CategoriaFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.guardadoCon) {
        uiState.guardadoCon?.let(onGuardado)
    }
    ContenidoFormCategoria(
        uiState = uiState,
        onNombreChange = viewModel::onNombreChange,
        onEdadMinChange = viewModel::onEdadMinChange,
        onEdadMaxChange = viewModel::onEdadMaxChange,
        onGuardar = viewModel::onGuardar,
        onMensajeMostrado = viewModel::onMensajeMostrado,
        onRegresar = onRegresar
    )
}

@Composable
fun ContenidoFormCategoria(
    uiState: CategoriaFormUiState,
    onNombreChange: (String) -> Unit = {},
    onEdadMinChange: (String) -> Unit = {},
    onEdadMaxChange: (String) -> Unit = {},
    onGuardar: () -> Unit = {},
    onMensajeMostrado: () -> Unit = {},
    onRegresar: () -> Unit = {}
) {
    val snackbar = remember { SnackbarHostState() }
    EfectoSnackbar(uiState.mensaje, snackbar, onMensajeMostrado)

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(if (uiState.esEdicion) R.string.editar_categoria else R.string.agregar_categoria),
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
                etiqueta = stringResource(R.string.nombre_categoria),
                error = uiState.errorNombre,
                ayuda = stringResource(R.string.ayuda_longitud, 3, 30),
                modifier = ancho
            )
            CampoTexto(
                valor = uiState.edadMin,
                onValorChange = onEdadMinChange,
                etiqueta = stringResource(R.string.edad_minima),
                error = uiState.errorEdadMin,
                teclado = KeyboardType.Number,
                modifier = ancho
            )
            CampoTexto(
                valor = uiState.edadMax,
                onValorChange = onEdadMaxChange,
                etiqueta = stringResource(R.string.edad_maxima),
                error = uiState.errorEdadMax,
                teclado = KeyboardType.Number,
                modifier = ancho
            )
            Text(
                stringResource(R.string.nota_categoria),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = ancho
            )
            BotonGuardar(uiState.guardando, onGuardar, ancho)
        }
    }
}

@Preview(showBackground = true, name = "Formulario categoría con error")
@Composable
fun PantallaFormCategoriaPreview() {
    CarrerasAtletismoTheme {
        ContenidoFormCategoria(
            uiState = CategoriaFormUiState(
                nombre = "Sub-23",
                edadMin = "23",
                edadMax = "18",
                errorEdadMax = MensajeUi(R.string.error_rango_edades)
            )
        )
    }
}
