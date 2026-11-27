package com.julio.carrerasatletismo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.julio.carrerasatletismo.R
import com.julio.carrerasatletismo.ui.theme.CarrerasAtletismoTheme

/**
 * Pantalla de inicio. No necesita ViewModel porque no tiene datos: solo recibe
 * lambdas de navegación (los eventos suben a NavegacionApp).
 */
@Composable
fun PantallaInicio(
    onVerCarreras: () -> Unit = {},
    onVerCorredores: () -> Unit = {},
    onVerCategorias: () -> Unit = {}
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // en horizontal se puede desplazar
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // painterResource(): carga una imagen de res/drawable
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = stringResource(R.string.logo_desc),
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // stringResource(): lee un texto de res/values/strings.xml
            Text(
                text = stringResource(R.string.titulo),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.subtitulo),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onVerCarreras,
                modifier = Modifier.fillMaxWidth().widthIn(max = 360.dp)
            ) { Text(stringResource(R.string.ver_carreras)) }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onVerCorredores,
                modifier = Modifier.fillMaxWidth().widthIn(max = 360.dp)
            ) { Text(stringResource(R.string.ver_corredores)) }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onVerCategorias,
                modifier = Modifier.fillMaxWidth().widthIn(max = 360.dp)
            ) { Text(stringResource(R.string.ver_categorias)) }
        }
    }
}

/** @Preview: Android Studio dibuja esta función en el panel de diseño, sin correr la app. */
@Preview(showBackground = true, name = "Inicio vertical")
@Composable
fun PantallaInicioPreview() {
    CarrerasAtletismoTheme { PantallaInicio() }
}

@Preview(showBackground = true, name = "Inicio horizontal", widthDp = 800, heightDp = 360)
@Composable
fun PantallaInicioHorizontalPreview() {
    CarrerasAtletismoTheme { PantallaInicio() }
}
