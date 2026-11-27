package com.julio.carrerasatletismo.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julio.carrerasatletismo.R
import com.julio.carrerasatletismo.domain.model.Carrera
import com.julio.carrerasatletismo.domain.model.Corredor
import com.julio.carrerasatletismo.domain.model.ErrorDominio
import com.julio.carrerasatletismo.domain.model.InscritoEnCarrera
import com.julio.carrerasatletismo.domain.normalizarTiempo
import com.julio.carrerasatletismo.domain.repository.CarrerasRepository
import com.julio.carrerasatletismo.ui.navigation.Rutas
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UDF: estado de la pantalla de detalle. */
data class DetalleCarreraUiState(
    val cargando: Boolean = true,
    val carrera: Carrera? = null,
    val inscritos: List<InscritoEnCarrera> = emptyList(),
    val corredores: List<Corredor> = emptyList(),
    val error: MensajeUi? = null,
    val mensaje: MensajeUi? = null,
    /** Error que se muestra DENTRO del diálogo abierto (ej. dorsal ocupado). */
    val errorDialogo: MensajeUi? = null,
    /** true cuando la operación del diálogo terminó bien y la pantalla debe cerrarlo. */
    val dialogoTerminado: Boolean = false,
    /** true cuando la carrera se eliminó y hay que regresar a la lista. */
    val eliminada: Boolean = false
)

private data class DatosDetalle(
    val carrera: Carrera?,
    val inscritos: List<InscritoEnCarrera>,
    val corredores: List<Corredor>,
    val error: MensajeUi? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CarreraDetalleViewModel @Inject constructor(
    private val repositorio: CarrerasRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /** El id llega en la ruta "detalle/{id}" y se lee con SavedStateHandle. */
    val idCarrera: String = savedStateHandle.get<String>(Rutas.ARG_ID).orEmpty()

    private val reintentos = MutableStateFlow(0)
    private val extra = MutableStateFlow(DetalleCarreraUiState(cargando = false))

    // OBSERVER: tres Flow de Room combinados; si se inscribe/borra a alguien la lista se actualiza sola.
    private val datos = reintentos.flatMapLatest {
        combine(
            repositorio.observarCarrera(idCarrera),
            repositorio.observarInscritos(idCarrera),
            repositorio.observarCorredores()
        ) { carrera, inscritos, corredores -> DatosDetalle(carrera, inscritos, corredores) }
            .catch { e -> emit(DatosDetalle(null, emptyList(), emptyList(), e.aMensajeUi())) }
    }

    val uiState: StateFlow<DetalleCarreraUiState> =
        combine(datos, extra) { d, e ->
            e.copy(
                cargando = false,
                carrera = d.carrera,
                inscritos = d.inscritos,
                corredores = d.corredores,
                error = d.error
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalleCarreraUiState())

    init {
        // Completa clima/recorrido desde la API. Si no hay internet se ignora: la app usa los datos locales.
        viewModelScope.launch {
            try {
                repositorio.cargarDetalleRemoto(idCarrera)
            } catch (e: Exception) {
                // Sin internet o error del servidor: no pasa nada, se muestra lo guardado en Room.
            }
        }
    }

    // ---------------- Eventos (UDF) ----------------

    fun onReintentar() = reintentos.update { it + 1 }

    fun onEliminarCarrera() {
        viewModelScope.launch {
            try {
                repositorio.eliminarCarrera(idCarrera)
                extra.update { it.copy(eliminada = true) }
            } catch (e: Exception) {
                extra.update { it.copy(mensaje = e.aMensajeUi()) }
            }
        }
    }

    /** Inserta (nueva) o actualiza (edición) la inscripción del corredor en esta carrera. */
    fun onGuardarInscripcion(idCorredor: Long?, dorsalTexto: String, tiempoTexto: String, esEdicion: Boolean) {
        val errorDorsal = Validaciones.entero(dorsalTexto, 1, 99_999)
        val errorTiempo = Validaciones.tiempo(tiempoTexto)
        when {
            idCorredor == null -> { extra.update { it.copy(errorDialogo = MensajeUi(R.string.error_elige_corredor)) }; return }
            errorDorsal != null -> { extra.update { it.copy(errorDialogo = errorDorsal) }; return }
            errorTiempo != null -> { extra.update { it.copy(errorDialogo = errorTiempo) }; return }
        }
        val dorsal = dorsalTexto.trim().toInt()
        val tiempo = normalizarTiempo(tiempoTexto)!!
        viewModelScope.launch {
            try {
                if (esEdicion) {
                    repositorio.actualizarInscripcion(idCorredor!!, idCarrera, dorsal, tiempo)
                    terminarDialogo(MensajeUi(R.string.msg_inscripcion_actualizada))
                } else {
                    repositorio.inscribir(idCorredor!!, idCarrera, dorsal, tiempo)
                    terminarDialogo(MensajeUi(R.string.msg_corredor_inscrito))
                }
            } catch (e: ErrorDominio) {
                // Error de negocio (dorsal ocupado, ya inscrito): se muestra dentro del diálogo
                extra.update { it.copy(errorDialogo = e.aMensajeUi()) }
            } catch (e: Exception) {
                extra.update { it.copy(errorDialogo = e.aMensajeUi()) }
            }
        }
    }

    fun onQuitarInscripcion(idCorredor: Long) {
        viewModelScope.launch {
            try {
                repositorio.quitarInscripcion(idCorredor, idCarrera)
                extra.update { it.copy(mensaje = MensajeUi(R.string.msg_inscripcion_eliminada)) }
            } catch (e: Exception) {
                extra.update { it.copy(mensaje = e.aMensajeUi()) }
            }
        }
    }

    fun onGuardarResultado(idCorredor: Long, tiempoTexto: String) {
        val error = Validaciones.tiempo(tiempoTexto)
        if (error != null) {
            extra.update { it.copy(errorDialogo = error) }
            return
        }
        viewModelScope.launch {
            try {
                repositorio.guardarResultado(idCorredor, idCarrera, normalizarTiempo(tiempoTexto)!!)
                terminarDialogo(MensajeUi(R.string.msg_resultado_guardado))
            } catch (e: Exception) {
                extra.update { it.copy(errorDialogo = e.aMensajeUi()) }
            }
        }
    }

    fun onBorrarResultado(idCorredor: Long) {
        viewModelScope.launch {
            try {
                repositorio.borrarResultado(idCorredor, idCarrera)
                terminarDialogo(MensajeUi(R.string.msg_resultado_borrado))
            } catch (e: Exception) {
                extra.update { it.copy(errorDialogo = e.aMensajeUi()) }
            }
        }
    }

    /** La pantalla avisa que ya cerró el diálogo (por éxito o porque el usuario canceló). */
    fun onDialogoCerrado() = extra.update { it.copy(dialogoTerminado = false, errorDialogo = null) }

    fun onMensajeMostrado() = extra.update { it.copy(mensaje = null) }

    private fun terminarDialogo(mensaje: MensajeUi) =
        extra.update { it.copy(dialogoTerminado = true, errorDialogo = null, mensaje = mensaje) }
}
