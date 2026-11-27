package com.julio.carrerasatletismo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.julio.carrerasatletismo.R
import com.julio.carrerasatletismo.domain.model.Carrera
import com.julio.carrerasatletismo.domain.model.Corredor
import com.julio.carrerasatletismo.domain.model.CorredorConCarreras
import com.julio.carrerasatletismo.ui.theme.CarrerasAtletismoTheme
import com.julio.carrerasatletismo.ui.viewmodel.CorredoresUiState
import com.julio.carrerasatletismo.ui.viewmodel.CorredoresViewModel
import com.julio.carrerasatletismo.ui.viewmodel.MensajeUi

@Composable
fun PantallaCorredores(
    mensajeExito: Int?,
    onMensajeExitoMostrado: () -> Unit,
    onRegresar: () -> Unit,
    onAgregar: () -> Unit,
    onEditar: (Long) -> Unit,
    viewModel: CorredoresViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    ContenidoCorredores(
        uiState = uiState,
        mensajeExito = mensajeExito?.let { MensajeUi(it) },
        onMensajeExitoMostrado = onMensajeExitoMostrado,
        onRegresar = onRegresar,
        onAgregar = onAgregar,
        onEditar = onEditar,
        onEliminar = viewModel::onEliminar,
        onReintentar = viewModel::onReintentar,
        onMensajeMostrado = viewModel::onMensajeMostrado
    )
}

@Composable
fun ContenidoCorredores(
    uiState: CorredoresUiState,
    mensajeExito: MensajeUi? = null,
    onMensajeExitoMostrado: () -> Unit = {},
    onRegresar: () -> Unit = {},
    onAgregar: () -> Unit = {},
    onEditar: (Long) -> Unit = {},
    onEliminar: (Long) -> Unit = {},
    onReintentar: () -> Unit = {},
    onMensajeMostrado: () -> Unit = {}
) {
    val snackbar = remember { SnackbarHostState() }
    EfectoSnackbar(uiState.mensaje, snackbar, onMensajeMostrado)
    EfectoSnackbar(mensajeExito, snackbar, onMensajeExitoMostrado)

    // Id del corredor que se quiere borrar (diálogo abierto). rememberSaveable → sobrevive al giro.
    var idAEliminar by rememberSaveable { mutableStateOf(-1L) }

    Scaffold(
        topBar = { BarraSuperior(stringResource(R.string.corredores), onRegresar) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAgregar) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.agregar_corredor))
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        val modificador = Modifier.padding(padding).consumeWindowInsets(padding).imePadding()
        when {
            uiState.cargando -> EstadoCargando(modificador)
            uiState.error != null -> EstadoError(uiState.error, onReintentar, modificador)
            uiState.corredores.isEmpty() -> EstadoVacio(stringResource(R.string.sin_corredores), modificador)
            else -> LazyColumn(
                modifier = modificador.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.corredores, key = { it.corredor.idCorredor }) { item ->
                    TarjetaCorredor(
                        item = item,
                        onEditar = { onEditar(item.corredor.idCorredor) },
                        onEliminar = { idAEliminar = item.corredor.idCorredor }
                    )
                }
            }
        }
    }

    val corredorAEliminar = uiState.corredores.firstOrNull { it.corredor.idCorredor == idAEliminar }
    if (corredorAEliminar != null) {
        DialogoConfirmarEliminar(
            titulo = stringResource(R.string.eliminar_corredor_titulo),
            mensaje = stringResource(
                R.string.eliminar_corredor_mensaje,
                corredorAEliminar.corredor.nombre,
                corredorAEliminar.carreras.size
            ),
            onConfirmar = {
                onEliminar(corredorAEliminar.corredor.idCorredor)
                idAEliminar = -1L
            },
            onCancelar = { idAEliminar = -1L }
        )
    }
}

@Composable
private fun TarjetaCorredor(item: CorredorConCarreras, onEditar: () -> Unit, onEliminar: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.corredor.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.edad_categoria, item.corredor.edad, item.corredor.categoria),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    if (item.carreras.isEmpty()) stringResource(R.string.sin_inscripciones)
                    else stringResource(R.string.inscrito_en, item.carreras.joinToString { it.nombre }),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.editar))
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.eliminar))
            }
        }
    }
}

@Preview(showBackground = true, name = "Corredores")
@Composable
fun PantallaCorredoresPreview() {
    CarrerasAtletismoTheme {
        ContenidoCorredores(
            uiState = CorredoresUiState(
                cargando = false,
                corredores = listOf(
                    CorredorConCarreras(
                        Corredor(1, "Ana López", 28, "Libre"),
                        listOf(Carrera("cdmx", "Maratón de la Ciudad de México", "2027-08-29", "Ciudad de México", "México", ""))
                    ),
                    CorredorConCarreras(Corredor(2, "Luis Pérez", 45, "Máster"), emptyList())
                )
            )
        )
    }
}
