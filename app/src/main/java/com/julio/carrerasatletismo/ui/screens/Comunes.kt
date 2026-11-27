package com.julio.carrerasatletismo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julio.carrerasatletismo.R
import com.julio.carrerasatletismo.ui.viewmodel.MensajeUi
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/** Convierte un MensajeUi en texto usando stringResource() (todos los textos están en strings.xml). */
@Composable
fun MensajeUi.texto(): String = stringResource(idTexto, *argumentos.toTypedArray())

/** Barra superior con botón de regresar, igual en todas las pantallas (Material 3). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(
    titulo: String,
    onRegresar: (() -> Unit)? = null,
    acciones: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = { Text(titulo, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            if (onRegresar != null) {
                IconButton(onClick = onRegresar) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.regresar)
                    )
                }
            }
        },
        actions = { acciones() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

/**
 * Muestra un Snackbar cuando llega un mensaje y avisa al ViewModel que ya se mostró.
 * Se usa en todas las pantallas para errores y mensajes de éxito.
 */
@Composable
fun EfectoSnackbar(mensaje: MensajeUi?, snackbar: SnackbarHostState, onMostrado: () -> Unit) {
    val texto = mensaje?.texto()
    val scope = rememberCoroutineScope()
    LaunchedEffect(texto) {
        if (texto != null) {
            onMostrado() // el ViewModel limpia el mensaje para no repetirlo
            scope.launch { snackbar.showSnackbar(texto) }
        }
    }
}

/** Estado de carga: círculo de progreso. */
@Composable
fun EstadoCargando(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** Estado de lista vacía (con scroll para que en horizontal no se corte). */
@Composable
fun EstadoVacio(texto: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(texto, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Estado de error con botón "Reintentar". */
@Composable
fun EstadoError(mensaje: MensajeUi, onReintentar: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(mensaje.texto(), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onReintentar) { Text(stringResource(R.string.reintentar)) }
    }
}

/** Diálogo de confirmación antes de eliminar (explica qué pasa con los registros relacionados). */
@Composable
fun DialogoConfirmarEliminar(
    titulo: String,
    mensaje: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
        title = { Text(titulo) },
        text = {
            Text(mensaje, modifier = Modifier.verticalScroll(rememberScrollState()))
        },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(stringResource(R.string.eliminar), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text(stringResource(R.string.cancelar)) }
        }
    )
}

/**
 * Campo de texto con manejo de error: isError pinta el campo de rojo y
 * supportingText muestra el mensaje personalizado debajo.
 */
@Composable
fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    error: MensajeUi?,
    modifier: Modifier = Modifier,
    ayuda: String? = null,
    teclado: KeyboardType = KeyboardType.Text,
    soloLectura: Boolean = false,
    iconoFinal: @Composable (() -> Unit)? = null
) {
    val textoApoyo: String? = error?.texto() ?: ayuda
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = if (textoApoyo != null) { { Text(textoApoyo) } } else null,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        singleLine = true,
        readOnly = soloLectura,
        trailingIcon = iconoFinal,
        modifier = modifier.fillMaxWidth()
    )
}

/** Botón "Guardar" que muestra un círculo de progreso mientras se guarda. */
@Composable
fun BotonGuardar(guardando: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        enabled = !guardando,
        modifier = modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
        if (guardando) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(stringResource(R.string.guardar))
    }
}

fun formatearFecha(fecha: String): String = try {
    val entrada = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val salida = SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("es-MX"))
    salida.format(entrada.parse(fecha)!!)
} catch (e: Exception) {
    fecha
}
