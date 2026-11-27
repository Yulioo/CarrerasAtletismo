package com.julio.carrerasatletismo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
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
import com.julio.carrerasatletismo.domain.model.Categoria
import com.julio.carrerasatletismo.ui.theme.CarrerasAtletismoTheme
import com.julio.carrerasatletismo.ui.viewmodel.CategoriasUiState
import com.julio.carrerasatletismo.ui.viewmodel.CategoriasViewModel
import com.julio.carrerasatletismo.ui.viewmodel.MensajeUi

/** Catálogo de categorías (CRUD de la entidad CATEGORIA). */
@Composable
fun PantallaCategorias(
    mensajeExito: Int?,
    onMensajeExitoMostrado: () -> Unit,
    onRegresar: () -> Unit,
    onAgregar: () -> Unit,
    onEditar: (Long) -> Unit,
    viewModel: CategoriasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    ContenidoCategorias(
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
fun ContenidoCategorias(
    uiState: CategoriasUiState,
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

    var idAEliminar by rememberSaveable { mutableStateOf(-1L) }

    Scaffold(
        topBar = { BarraSuperior(stringResource(R.string.categorias), onRegresar) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAgregar) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.agregar_categoria))
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        val modificador = Modifier.padding(padding).consumeWindowInsets(padding)
        when {
            uiState.cargando -> EstadoCargando(modificador)
            uiState.error != null -> EstadoError(uiState.error, onReintentar, modificador)
            uiState.categorias.isEmpty() -> EstadoVacio(stringResource(R.string.sin_categorias), modificador)
            else -> LazyColumn(
                modifier = modificador.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.categorias, key = { it.idCategoria }) { categoria ->
                    TarjetaCategoria(
                        categoria = categoria,
                        onEditar = { onEditar(categoria.idCategoria) },
                        onEliminar = { idAEliminar = categoria.idCategoria }
                    )
                }
            }
        }
    }

    val categoriaAEliminar = uiState.categorias.firstOrNull { it.idCategoria == idAEliminar }
    if (categoriaAEliminar != null) {
        DialogoConfirmarEliminar(
            titulo = stringResource(R.string.eliminar_categoria_titulo),
            mensaje = if (categoriaAEliminar.totalCorredores > 0)
                stringResource(R.string.eliminar_categoria_con_corredores, categoriaAEliminar.nombre, categoriaAEliminar.totalCorredores)
            else
                stringResource(R.string.eliminar_categoria_mensaje, categoriaAEliminar.nombre),
            onConfirmar = {
                onEliminar(categoriaAEliminar.idCategoria)
                idAEliminar = -1L
            },
            onCancelar = { idAEliminar = -1L }
        )
    }
}

@Composable
private fun TarjetaCategoria(categoria: Categoria, onEditar: () -> Unit, onEliminar: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(categoria.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.rango_edades, categoria.edadMin, categoria.edadMax),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    stringResource(R.string.total_corredores, categoria.totalCorredores),
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

@Preview(showBackground = true, name = "Categorías")
@Composable
fun PantallaCategoriasPreview() {
    CarrerasAtletismoTheme {
        ContenidoCategorias(
            uiState = CategoriasUiState(
                cargando = false,
                categorias = listOf(
                    Categoria(1, "Juvenil", 0, 17, 1),
                    Categoria(2, "Libre", 18, 39, 4),
                    Categoria(3, "Máster", 40, 999, 2)
                )
            )
        )
    }
}
