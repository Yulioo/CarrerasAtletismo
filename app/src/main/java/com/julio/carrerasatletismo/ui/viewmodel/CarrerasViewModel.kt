package com.julio.carrerasatletismo.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julio.carrerasatletismo.R
import com.julio.carrerasatletismo.domain.model.Carrera
import com.julio.carrerasatletismo.domain.repository.CarrerasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UDF: TODO lo que la pantalla de la lista necesita para dibujarse. */
data class CarrerasUiState(
    val busqueda: String = "",
    val carreras: List<Carrera> = emptyList(),
    val cargando: Boolean = true,
    val sincronizando: Boolean = false,
    val error: MensajeUi? = null,
    val mensaje: MensajeUi? = null
)

/**
 * ViewModel de la lista de carreras.
 * - @HiltViewModel + @Inject constructor: Hilt le inyecta el repositorio (Inyección de dependencias).
 * - Solo conoce la INTERFAZ CarrerasRepository (patrón Repository).
 * - Expone un StateFlow<CarrerasUiState> que la pantalla observa (patrón Observer).
 * - Recibe eventos (onBusquedaChange, onSincronizar…) y actualiza el estado (UDF).
 * - Al girar la pantalla el ViewModel NO se destruye, así que el estado se conserva.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CarrerasViewModel @Inject constructor(
    private val repositorio: CarrerasRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // El texto del buscador se guarda en SavedStateHandle (sobrevive incluso si Android cierra el proceso)
    private val busqueda = savedStateHandle.getStateFlow(CLAVE_BUSQUEDA, "")
    private val reintentos = MutableStateFlow(0)
    private val sincronizando = MutableStateFlow(false)
    private val mensaje = MutableStateFlow<MensajeUi?>(null)

    // OBSERVER: Room (Flow) → aquí se transforma → StateFlow que observa la vista.
    private val lista = combine(busqueda, reintentos) { texto, _ -> texto }
        .flatMapLatest { texto ->
            repositorio.observarCarreras(texto)
                .map<List<Carrera>, EstadoLista<Carrera>> { EstadoLista.Listo(it) }
                .catch { e -> emit(EstadoLista.Error(e.aMensajeUi())) }
        }

    val uiState: StateFlow<CarrerasUiState> =
        combine(busqueda, lista, sincronizando, mensaje) { texto, estado, sinc, msg ->
            CarrerasUiState(
                busqueda = texto,
                carreras = if (estado is EstadoLista.Listo<Carrera>) estado.datos else emptyList(),
                cargando = estado is EstadoLista.Cargando,
                sincronizando = sinc,
                error = if (estado is EstadoLista.Error) estado.mensaje else null,
                mensaje = msg
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CarrerasUiState(busqueda = busqueda.value)
        )

    init {
        // Solo se descarga de la API si la base de datos está vacía (no se pierden datos del usuario)
        viewModelScope.launch {
            try {
                if (!repositorio.hayCarreras()) onSincronizar()
            } catch (e: Exception) {
                mensaje.value = e.aMensajeUi()
            }
        }
    }

    // ---------------- Eventos que suben desde la pantalla (UDF) ----------------

    fun onBusquedaChange(texto: String) {
        savedStateHandle[CLAVE_BUSQUEDA] = texto.take(50)
    }

    fun onReintentar() {
        reintentos.update { it + 1 }
    }

    /** suspend fun del repositorio lanzada con viewModelScope.launch; try/catch para que la app nunca se cierre. */
    fun onSincronizar() {
        if (sincronizando.value) return
        viewModelScope.launch {
            sincronizando.value = true
            try {
                val total = repositorio.sincronizarCarreras()
                mensaje.value = MensajeUi(R.string.msg_carreras_descargadas, listOf(total))
            } catch (e: Exception) {
                mensaje.value = e.aMensajeUi()
            } finally {
                sincronizando.value = false
            }
        }
    }

    fun onMensajeMostrado() {
        mensaje.value = null
    }

    private companion object {
        const val CLAVE_BUSQUEDA = "busqueda"
    }
}
