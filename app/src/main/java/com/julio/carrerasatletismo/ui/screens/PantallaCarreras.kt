package com.julio.carrerasatletismo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.julio.carrerasatletismo.R
import com.julio.carrerasatletismo.domain.model.Carrera
import com.julio.carrerasatletismo.ui.theme.CarrerasAtletismoTheme
import com.julio.carrerasatletismo.ui.viewmodel.CarrerasUiState
import com.julio.carrerasatletismo.ui.viewmodel.CarrerasViewModel
import com.julio.carrerasatletismo.ui.viewmodel.MensajeUi

/**
 * Pantalla "con estado": obtiene el ViewModel con hiltViewModel(), observa su StateFlow con
 * collectAsState() (Observer) y le pasa estado + eventos a la pantalla "sin estado" (UDF).
 */
@Composable
fun PantallaCarreras(
    mensajeExito: Int?,
    onMensajeExitoMostrado: () -> Unit,
    onRegresar: () -> Unit,
    onAbrirCarrera: (String) -> Unit,
    onAgregar: () -> Unit,
    viewModel: CarrerasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    ContenidoCarreras(
        uiState = uiState,
        mensajeExito = mensajeExito?.let { MensajeUi(it) },
        onMensajeExitoMostrado = onMensajeExitoMostrado,
        onBusquedaChange = viewModel::onBusquedaChange,
        onSincronizar = viewModel::onSincronizar,
        onReintentar = viewModel::onReintentar,
        onMensajeMostrado = viewModel::onMensajeMostrado,
        onRegresar = onRegresar,
        onAbrirCarrera = onAbrirCarrera,
        onAgregar = onAgregar
    )
}

/** Pantalla "sin estado": solo dibuja lo que recibe y avisa los eventos con lambdas. */
@Composable
fun ContenidoCarreras(
    uiState: CarrerasUiState,
    mensajeExito: MensajeUi? = null,
    onMensajeExitoMostrado: () -> Unit = {},
    onBusquedaChange: (String) -> Unit = {},
    onSincronizar: () -> Unit = {},
    onReintentar: () -> Unit = {},
    onMensajeMostrado: () -> Unit = {},
    onRegresar: () -> Unit = {},
    onAbrirCarrera: (String) -> Unit = {},
    onAgregar: () -> Unit = {}
) {
    val snackbar = remember { SnackbarHostState() }
    EfectoSnackbar(uiState.mensaje, snackbar, onMensajeMostrado)
    EfectoSnackbar(mensajeExito, snackbar, onMensajeExitoMostrado)

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.carreras),
                onRegresar = onRegresar,
                acciones = {
                    IconButton(onClick = onSincronizar, enabled = !uiState.sincronizando) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.actualizar))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAgregar) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.agregar_carrera))
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .fillMaxSize()
        ) {
            if (uiState.sincronizando) LinearProgressIndicator(Modifier.fillMaxWidth())

            // Buscador
            OutlinedTextField(
                value = uiState.busqueda,
                onValueChange = onBusquedaChange,
                placeholder = { Text(stringResource(R.string.buscar_carreras)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.busqueda.isNotEmpty()) {
                        IconButton(onClick = { onBusquedaChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.limpiar_busqueda))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when {
                uiState.cargando -> EstadoCargando()
                uiState.error != null -> EstadoError(uiState.error, onReintentar)
                uiState.carreras.isEmpty() -> EstadoVacio(
                    if (uiState.busqueda.isBlank()) stringResource(R.string.sin_carreras)
                    else stringResource(R.string.sin_resultados, uiState.busqueda)
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.carreras, key = { it.idCarrera }) { carrera ->
                        TarjetaCarrera(carrera) { onAbrirCarrera(carrera.idCarrera) }
                    }
                    item {
                        Text(
                            stringResource(R.string.atribucion),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaCarrera(carrera: Carrera, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val partes = formatearFecha(carrera.fecha).split(" ")
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            partes.getOrElse(0) { "" },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            partes.getOrElse(1) { "" }.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(carrera.nombre, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        listOf(carrera.ciudad, carrera.pais).filter { it.isNotBlank() }.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    formatearFecha(carrera.fecha),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

// ---------------- @Preview con datos de ejemplo ----------------

private val carrerasEjemplo = listOf(
    Carrera("tokyo2027", "Maratón de Tokio", "2027-03-07", "Tokio", "Japón", ""),
    Carrera("boston2027", "Maratón de Boston", "2027-04-19", "Boston", "Estados Unidos", ""),
    Carrera("cdmx2027", "Maratón de la Ciudad de México", "2027-08-29", "Ciudad de México", "México", "")
)

@Preview(showBackground = true, name = "Lista de carreras")
@Composable
fun PantallaCarrerasPreview() {
    CarrerasAtletismoTheme {
        ContenidoCarreras(uiState = CarrerasUiState(carreras = carrerasEjemplo, cargando = false))
    }
}

@Preview(showBackground = true, name = "Lista vacía")
@Composable
fun PantallaCarrerasVaciaPreview() {
    CarrerasAtletismoTheme {
        ContenidoCarreras(uiState = CarrerasUiState(busqueda = "Roma", cargando = false))
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
fun PantallaCarrerasErrorPreview() {
    CarrerasAtletismoTheme {
        ContenidoCarreras(uiState = CarrerasUiState(cargando = false, error = MensajeUi(R.string.error_inesperado)))
    }
}
