package com.julio.carrerasatletismo.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.julio.carrerasatletismo.R
import com.julio.carrerasatletismo.domain.model.Carrera
import com.julio.carrerasatletismo.domain.model.Corredor
import com.julio.carrerasatletismo.domain.model.InscritoEnCarrera
import com.julio.carrerasatletismo.ui.theme.CarrerasAtletismoTheme
import com.julio.carrerasatletismo.ui.viewmodel.CarreraDetalleViewModel
import com.julio.carrerasatletismo.ui.viewmodel.DetalleCarreraUiState
import com.julio.carrerasatletismo.ui.viewmodel.MensajeUi

/** Pantalla con estado: conecta el ViewModel con la UI sin estado. */
@Composable
fun PantallaDetalleCarrera(
    mensajeExito: Int?,
    onMensajeExitoMostrado: () -> Unit,
    onRegresar: () -> Unit,
    onEditar: (String) -> Unit,
    onEliminada: () -> Unit,
    viewModel: CarreraDetalleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Cuando la carrera se eliminó, regresamos a la lista (el NavHost muestra el mensaje allá)
    LaunchedEffect(uiState.eliminada) {
        if (uiState.eliminada) onEliminada()
    }

    ContenidoDetalleCarrera(
        uiState = uiState,
        mensajeExito = mensajeExito?.let { MensajeUi(it) },
        onMensajeExitoMostrado = onMensajeExitoMostrado,
        onRegresar = onRegresar,
        onEditar = { onEditar(viewModel.idCarrera) },
        onEliminar = viewModel::onEliminarCarrera,
        onGuardarInscripcion = viewModel::onGuardarInscripcion,
        onQuitarInscripcion = viewModel::onQuitarInscripcion,
        onGuardarResultado = viewModel::onGuardarResultado,
        onBorrarResultado = viewModel::onBorrarResultado,
        onDialogoCerrado = viewModel::onDialogoCerrado,
        onReintentar = viewModel::onReintentar,
        onMensajeMostrado = viewModel::onMensajeMostrado
    )
}

/** Qué diálogo está abierto. Se guarda como texto con rememberSaveable para sobrevivir al giro. */
private object Dialogo {
    const val NINGUNO = ""
    const val INSCRIBIR = "inscribir"
    const val EDITAR_INSCRIPCION = "editar"
    const val RESULTADO = "resultado"
    const val QUITAR = "quitar"
    const val ELIMINAR_CARRERA = "eliminar"
}

@Composable
fun ContenidoDetalleCarrera(
    uiState: DetalleCarreraUiState,
    mensajeExito: MensajeUi? = null,
    onMensajeExitoMostrado: () -> Unit = {},
    onRegresar: () -> Unit = {},
    onEditar: () -> Unit = {},
    onEliminar: () -> Unit = {},
    onGuardarInscripcion: (Long?, String, String, Boolean) -> Unit = { _, _, _, _ -> },
    onQuitarInscripcion: (Long) -> Unit = {},
    onGuardarResultado: (Long, String) -> Unit = { _, _ -> },
    onBorrarResultado: (Long) -> Unit = {},
    onDialogoCerrado: () -> Unit = {},
    onReintentar: () -> Unit = {},
    onMensajeMostrado: () -> Unit = {}
) {
    val snackbar = remember { SnackbarHostState() }
    EfectoSnackbar(uiState.mensaje, snackbar, onMensajeMostrado)
    EfectoSnackbar(mensajeExito, snackbar, onMensajeExitoMostrado)

    // Estado LOCAL de la UI con rememberSaveable: sobrevive a la rotación de pantalla
    var dialogo by rememberSaveable { mutableStateOf(Dialogo.NINGUNO) }
    var idCorredorDialogo by rememberSaveable { mutableStateOf(-1L) }

    fun cerrarDialogo() {
        dialogo = Dialogo.NINGUNO
        idCorredorDialogo = -1L
        onDialogoCerrado()
    }

    // El ViewModel avisa que la operación del diálogo terminó bien → se cierra
    LaunchedEffect(uiState.dialogoTerminado) {
        if (uiState.dialogoTerminado) cerrarDialogo()
    }

    val carrera = uiState.carrera

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = carrera?.nombre ?: stringResource(R.string.detalle),
                onRegresar = onRegresar,
                acciones = {
                    if (carrera != null) {
                        IconButton(onClick = onEditar) {
                            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.editar))
                        }
                        IconButton(onClick = { dialogo = Dialogo.ELIMINAR_CARRERA }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.eliminar))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (carrera != null) {
                ExtendedFloatingActionButton(
                    onClick = { dialogo = Dialogo.INSCRIBIR },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.inscribir)) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        val contenidoModifier = Modifier
            .padding(padding)
            .consumeWindowInsets(padding)
            .imePadding()
        when {
            uiState.cargando -> EstadoCargando(contenidoModifier)
            uiState.error != null -> EstadoError(uiState.error, onReintentar, contenidoModifier)
            carrera == null -> EstadoVacio(stringResource(R.string.carrera_no_encontrada), contenidoModifier)
            else -> ListaDetalle(
                carrera = carrera,
                inscritos = uiState.inscritos,
                modifier = contenidoModifier,
                onEditar = onEditar,
                onEliminar = { dialogo = Dialogo.ELIMINAR_CARRERA },
                onEditarInscripcion = { id -> idCorredorDialogo = id; dialogo = Dialogo.EDITAR_INSCRIPCION },
                onResultado = { id -> idCorredorDialogo = id; dialogo = Dialogo.RESULTADO },
                onQuitar = { id -> idCorredorDialogo = id; dialogo = Dialogo.QUITAR }
            )
        }
    }

    // ---------------- Diálogos ----------------
    val inscritoDialogo = uiState.inscritos.firstOrNull { it.idCorredor == idCorredorDialogo }
    when (dialogo) {
        Dialogo.ELIMINAR_CARRERA -> DialogoConfirmarEliminar(
            titulo = stringResource(R.string.eliminar_carrera_titulo),
            mensaje = stringResource(R.string.eliminar_carrera_mensaje, carrera?.nombre ?: "", uiState.inscritos.size),
            onConfirmar = { cerrarDialogo(); onEliminar() },
            onCancelar = { cerrarDialogo() }
        )

        Dialogo.QUITAR -> if (inscritoDialogo != null) {
            DialogoConfirmarEliminar(
                titulo = stringResource(R.string.quitar_inscripcion),
                mensaje = stringResource(R.string.quitar_inscripcion_mensaje, inscritoDialogo.nombre),
                onConfirmar = { onQuitarInscripcion(inscritoDialogo.idCorredor); cerrarDialogo() },
                onCancelar = { cerrarDialogo() }
            )
        }

        Dialogo.INSCRIBIR -> DialogoInscripcion(
            corredores = uiState.corredores.filter { c -> uiState.inscritos.none { it.idCorredor == c.idCorredor } },
            inscrito = null,
            error = uiState.errorDialogo,
            onCancelar = { cerrarDialogo() },
            onGuardar = { idCorredor, dorsal, tiempo -> onGuardarInscripcion(idCorredor, dorsal, tiempo, false) }
        )

        Dialogo.EDITAR_INSCRIPCION -> if (inscritoDialogo != null) {
            DialogoInscripcion(
                corredores = emptyList(),
                inscrito = inscritoDialogo,
                error = uiState.errorDialogo,
                onCancelar = { cerrarDialogo() },
                onGuardar = { idCorredor, dorsal, tiempo -> onGuardarInscripcion(idCorredor, dorsal, tiempo, true) }
            )
        }

        Dialogo.RESULTADO -> if (inscritoDialogo != null) {
            DialogoResultado(
                inscrito = inscritoDialogo,
                error = uiState.errorDialogo,
                onCancelar = { cerrarDialogo() },
                onGuardar = { tiempo -> onGuardarResultado(inscritoDialogo.idCorredor, tiempo) },
                onBorrar = { onBorrarResultado(inscritoDialogo.idCorredor) }
            )
        }
    }
}

@Composable
private fun ListaDetalle(
    carrera: Carrera,
    inscritos: List<InscritoEnCarrera>,
    modifier: Modifier,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onEditarInscripcion: (Long) -> Unit,
    onResultado: (Long) -> Unit,
    onQuitar: (Long) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { TarjetaInfo(carrera, onEditar, onEliminar) }
        item { TarjetaClimaRecorrido(carrera) }
        if (carrera.url.isNotBlank()) {
            item {
                TextButton(onClick = { runCatching { uriHandler.openUri(carrera.url) } }) {
                    Text(stringResource(R.string.mas_info))
                }
            }
        }
        item {
            Text(
                stringResource(R.string.inscritos_conteo, inscritos.size),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (inscritos.isEmpty()) {
            item {
                Text(stringResource(R.string.sin_inscritos), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(inscritos, key = { it.idCorredor }) { ins ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.dorsal_formato, ins.dorsal),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(ins.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.categoria_meta, ins.categoria, ins.tiempoObjetivo),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (ins.tiempoReal != null) {
                            Text(
                                stringResource(R.string.resultado_formato, ins.posicion ?: 0, ins.tiempoReal),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { onResultado(ins.idCorredor) }) {
                        Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.registrar_resultado))
                    }
                    IconButton(onClick = { onEditarInscripcion(ins.idCorredor) }) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.editar_inscripcion))
                    }
                    IconButton(onClick = { onQuitar(ins.idCorredor) }) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.quitar_inscripcion))
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaInfo(c: Carrera, onEditar: () -> Unit, onEliminar: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(c.nombre, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.fecha_formato, formatearFecha(c.fecha)))
            Text(stringResource(R.string.lugar_formato, listOf(c.ciudad, c.pais).filter { it.isNotBlank() }.joinToString(", ")))
            Text(
                stringResource(if (c.esLocal) R.string.origen_local else R.string.origen_api),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEditar) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.editar))
                }
                Button(
                    onClick = onEliminar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.eliminar))
                }
            }
        }
    }
}

@Composable
private fun TarjetaClimaRecorrido(c: Carrera) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.clima), style = MaterialTheme.typography.titleMedium)
            val sinDato = stringResource(R.string.sin_dato)
            val temperatura = c.temperaturaC?.let { stringResource(R.string.temperatura_valor, it) } ?: sinDato
            val humedad = c.humedad?.let { stringResource(R.string.humedad_valor, it) } ?: sinDato
            Text(stringResource(R.string.clima_formato, temperatura, humedad))
            Text(stringResource(R.string.condiciones_formato, c.condiciones ?: sinDato))
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text(stringResource(R.string.recorrido), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.tipo_formato, c.tipoRecorrido ?: sinDato))
            Text(stringResource(R.string.desnivel_formato, c.desnivelM?.let { stringResource(R.string.desnivel_valor, it) } ?: sinDato))
            Text(stringResource(R.string.terreno_formato, c.terreno ?: sinDato))
        }
    }
}

/**
 * Diálogo para INSCRIBIR (nuevo) o EDITAR una inscripción.
 * Los textos que escribe el usuario usan rememberSaveable: al girar la pantalla no se pierden.
 */
@Composable
private fun DialogoInscripcion(
    corredores: List<Corredor>,
    inscrito: InscritoEnCarrera?,
    error: MensajeUi?,
    onCancelar: () -> Unit,
    onGuardar: (idCorredor: Long?, dorsal: String, tiempo: String) -> Unit
) {
    val esEdicion = inscrito != null
    var seleccionado by rememberSaveable { mutableStateOf(inscrito?.idCorredor ?: -1L) }
    var dorsal by rememberSaveable { mutableStateOf(inscrito?.dorsal?.toString() ?: "") }
    var tiempo by rememberSaveable { mutableStateOf(inscrito?.tiempoObjetivo ?: "") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(if (esEdicion) R.string.editar_inscripcion else R.string.inscribir)) },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                if (esEdicion) {
                    Text(inscrito!!.nombre, style = MaterialTheme.typography.titleMedium)
                } else if (corredores.isEmpty()) {
                    Text(stringResource(R.string.sin_corredores_inscribir))
                } else {
                    Text(stringResource(R.string.elige_corredor), style = MaterialTheme.typography.labelLarge)
                    corredores.forEach { corredor ->
                        Row(
                            Modifier.fillMaxWidth().clickable { seleccionado = corredor.idCorredor },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = seleccionado == corredor.idCorredor,
                                onClick = { seleccionado = corredor.idCorredor }
                            )
                            Text(stringResource(R.string.corredor_categoria, corredor.nombre, corredor.categoria))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                CampoTexto(
                    valor = dorsal,
                    onValorChange = { dorsal = it.filter(Char::isDigit).take(5) },
                    etiqueta = stringResource(R.string.dorsal),
                    error = null,
                    teclado = KeyboardType.Number
                )
                CampoTexto(
                    valor = tiempo,
                    onValorChange = { tiempo = it.filter { c -> c.isDigit() || c == ':' }.take(8) },
                    etiqueta = stringResource(R.string.tiempo_objetivo),
                    error = null,
                    ayuda = stringResource(R.string.ayuda_tiempo),
                    teclado = KeyboardType.Number
                )
                if (error != null) {
                    Text(error.texto(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = esEdicion || corredores.isNotEmpty(),
                onClick = {
                    val id = if (esEdicion) inscrito!!.idCorredor else seleccionado.takeIf { it != -1L }
                    onGuardar(id, dorsal, tiempo)
                }
            ) { Text(stringResource(R.string.guardar)) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text(stringResource(R.string.cancelar)) }
        }
    )
}

@Composable
private fun DialogoResultado(
    inscrito: InscritoEnCarrera,
    error: MensajeUi?,
    onCancelar: () -> Unit,
    onGuardar: (String) -> Unit,
    onBorrar: () -> Unit
) {
    var tiempo by rememberSaveable { mutableStateOf(inscrito.tiempoReal ?: "") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(R.string.registrar_resultado)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(inscrito.nombre, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                CampoTexto(
                    valor = tiempo,
                    onValorChange = { tiempo = it.filter { c -> c.isDigit() || c == ':' }.take(8) },
                    etiqueta = stringResource(R.string.tiempo_real),
                    error = error,
                    ayuda = stringResource(R.string.ayuda_tiempo),
                    teclado = KeyboardType.Number
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onGuardar(tiempo) }) { Text(stringResource(R.string.guardar)) }
        },
        dismissButton = {
            Row {
                if (inscrito.tiempoReal != null) {
                    TextButton(onClick = onBorrar) { Text(stringResource(R.string.borrar_resultado)) }
                }
                TextButton(onClick = onCancelar) { Text(stringResource(R.string.cancelar)) }
            }
        }
    )
}

// ---------------- @Preview con datos de ejemplo ----------------

@Preview(showBackground = true, name = "Detalle de carrera")
@Composable
fun PantallaDetalleCarreraPreview() {
    CarrerasAtletismoTheme {
        ContenidoDetalleCarrera(
            uiState = DetalleCarreraUiState(
                cargando = false,
                carrera = Carrera(
                    idCarrera = "local-cdmx-2027",
                    nombre = "Maratón de la Ciudad de México",
                    fecha = "2027-08-29",
                    ciudad = "Ciudad de México",
                    pais = "México",
                    url = "",
                    tipoRecorrido = "loop",
                    desnivelM = 120,
                    terreno = "road",
                    temperaturaC = 14.5,
                    humedad = 70,
                    condiciones = "Fresco"
                ),
                inscritos = listOf(
                    InscritoEnCarrera(1, "Ana López", "Libre", 101, "03:45:00", "03:41:22", 1),
                    InscritoEnCarrera(2, "Luis Pérez", "Máster", 102, "04:10:00")
                )
            )
        )
    }
}
