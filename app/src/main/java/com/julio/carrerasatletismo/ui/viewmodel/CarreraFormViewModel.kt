package com.julio.carrerasatletismo.ui.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julio.carrerasatletismo.R
import com.julio.carrerasatletismo.domain.model.Carrera
import com.julio.carrerasatletismo.domain.model.ErrorDominio
import com.julio.carrerasatletismo.domain.repository.CarrerasRepository
import com.julio.carrerasatletismo.ui.navigation.Rutas
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UDF: estado del formulario de carrera (agregar / editar). */
data class CarreraFormUiState(
    val esEdicion: Boolean = false,
    val cargando: Boolean = false,
    val nombre: String = "",
    val fecha: String = "",
    val ciudad: String = "",
    val pais: String = "",
    val url: String = "",
    val errorNombre: MensajeUi? = null,
    val errorFecha: MensajeUi? = null,
    val errorCiudad: MensajeUi? = null,
    val errorPais: MensajeUi? = null,
    val errorUrl: MensajeUi? = null,
    val guardando: Boolean = false,
    val mensaje: MensajeUi? = null,
    /** Cuando no es null, se guardó bien: la pantalla regresa y muestra este mensaje de éxito. */
    @StringRes val guardadoCon: Int? = null
)

@HiltViewModel
class CarreraFormViewModel @Inject constructor(
    private val repositorio: CarrerasRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /** Ruta "formulario_carrera?id={id}": si id es null es una carrera NUEVA, si no, se EDITA. */
    private val idCarrera: String? = savedStateHandle.get<String>(Rutas.ARG_ID)?.takeIf { it.isNotBlank() }
    private var original: Carrera? = null

    private val _uiState = MutableStateFlow(CarreraFormUiState(esEdicion = idCarrera != null))
    val uiState: StateFlow<CarreraFormUiState> = _uiState.asStateFlow()

    init {
        if (idCarrera != null) cargar(idCarrera)
    }

    private fun cargar(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            try {
                val carrera = repositorio.obtenerCarrera(id) ?: throw ErrorDominio.NoEncontrado()
                original = carrera
                _uiState.update {
                    it.copy(
                        cargando = false,
                        nombre = carrera.nombre,
                        fecha = carrera.fecha,
                        ciudad = carrera.ciudad,
                        pais = carrera.pais,
                        url = carrera.url
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, mensaje = e.aMensajeUi()) }
            }
        }
    }

    // ---------------- Eventos (UDF): la vista NO modifica el estado, solo avisa ----------------
    fun onNombreChange(valor: String) = _uiState.update { it.copy(nombre = valor.take(80), errorNombre = null) }
    fun onFechaChange(valor: String) = _uiState.update { it.copy(fecha = valor, errorFecha = null) }
    fun onCiudadChange(valor: String) = _uiState.update { it.copy(ciudad = valor.take(50), errorCiudad = null) }
    fun onPaisChange(valor: String) = _uiState.update { it.copy(pais = valor.take(50), errorPais = null) }
    fun onUrlChange(valor: String) = _uiState.update { it.copy(url = valor.take(200), errorUrl = null) }
    fun onMensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    fun onGuardar() {
        val s = _uiState.value
        if (s.guardando) return

        // 1) Validación de campos
        val errorNombre = Validaciones.texto(s.nombre, 3, 80)
        val errorFecha = Validaciones.fecha(s.fecha)
        val errorCiudad = Validaciones.texto(s.ciudad, 2, 50)
        val errorPais = Validaciones.texto(s.pais, 2, 50)
        val errorUrl = Validaciones.url(s.url)
        if (listOf(errorNombre, errorFecha, errorCiudad, errorPais, errorUrl).any { it != null }) {
            _uiState.update {
                it.copy(
                    errorNombre = errorNombre, errorFecha = errorFecha, errorCiudad = errorCiudad,
                    errorPais = errorPais, errorUrl = errorUrl,
                    mensaje = MensajeUi(R.string.error_revisa_campos)
                )
            }
            return
        }

        // 2) Guardar en Room (suspend fun) dentro de viewModelScope.launch
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            try {
                val carrera = (original ?: Carrera("", "", "", "", "", "")).copy(
                    nombre = s.nombre.trim(),
                    fecha = s.fecha.trim(),
                    ciudad = s.ciudad.trim(),
                    pais = s.pais.trim(),
                    url = s.url.trim()
                )
                if (original != null) {
                    repositorio.actualizarCarrera(carrera)
                    _uiState.update { it.copy(guardando = false, guardadoCon = R.string.msg_carrera_actualizada) }
                } else {
                    repositorio.agregarCarrera(carrera)
                    _uiState.update { it.copy(guardando = false, guardadoCon = R.string.msg_carrera_agregada) }
                }
            } catch (e: ErrorDominio.NombreDuplicado) {
                val msg = MensajeUi(R.string.error_carrera_duplicada)
                _uiState.update { it.copy(guardando = false, errorNombre = msg, mensaje = msg) }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, mensaje = e.aMensajeUi()) }
            }
        }
    }
}
