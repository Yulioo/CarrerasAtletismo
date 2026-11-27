package com.julio.carrerasatletismo.ui.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julio.carrerasatletismo.R
import com.julio.carrerasatletismo.domain.model.Categoria
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

data class CategoriaFormUiState(
    val esEdicion: Boolean = false,
    val cargando: Boolean = false,
    val nombre: String = "",
    val edadMin: String = "",
    val edadMax: String = "",
    val errorNombre: MensajeUi? = null,
    val errorEdadMin: MensajeUi? = null,
    val errorEdadMax: MensajeUi? = null,
    val guardando: Boolean = false,
    val mensaje: MensajeUi? = null,
    @StringRes val guardadoCon: Int? = null
)

@HiltViewModel
class CategoriaFormViewModel @Inject constructor(
    private val repositorio: CarrerasRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val idCategoria: Long = savedStateHandle.get<Long>(Rutas.ARG_ID) ?: Rutas.SIN_ID

    private val _uiState = MutableStateFlow(CategoriaFormUiState(esEdicion = idCategoria != Rutas.SIN_ID))
    val uiState: StateFlow<CategoriaFormUiState> = _uiState.asStateFlow()

    init {
        if (idCategoria != Rutas.SIN_ID) {
            viewModelScope.launch {
                _uiState.update { it.copy(cargando = true) }
                try {
                    val categoria = repositorio.obtenerCategoria(idCategoria) ?: throw ErrorDominio.NoEncontrado()
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            nombre = categoria.nombre,
                            edadMin = categoria.edadMin.toString(),
                            edadMax = categoria.edadMax.toString()
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(cargando = false, mensaje = e.aMensajeUi()) }
                }
            }
        }
    }

    // ---------------- Eventos (UDF) ----------------
    fun onNombreChange(valor: String) = _uiState.update { it.copy(nombre = valor.take(30), errorNombre = null) }
    fun onEdadMinChange(valor: String) =
        _uiState.update { it.copy(edadMin = valor.filter(Char::isDigit).take(3), errorEdadMin = null) }
    fun onEdadMaxChange(valor: String) =
        _uiState.update { it.copy(edadMax = valor.filter(Char::isDigit).take(3), errorEdadMax = null) }
    fun onMensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    fun onGuardar() {
        val s = _uiState.value
        if (s.guardando) return
        val errorNombre = Validaciones.texto(s.nombre, 3, 30)
        val errorMin = Validaciones.entero(s.edadMin, 0, 120)
        var errorMax = Validaciones.entero(s.edadMax, 0, 999)
        if (errorMin == null && errorMax == null && s.edadMax.trim().toInt() < s.edadMin.trim().toInt()) {
            errorMax = MensajeUi(R.string.error_rango_edades)
        }
        if (errorNombre != null || errorMin != null || errorMax != null) {
            _uiState.update {
                it.copy(
                    errorNombre = errorNombre, errorEdadMin = errorMin, errorEdadMax = errorMax,
                    mensaje = MensajeUi(R.string.error_revisa_campos)
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            try {
                val min = s.edadMin.trim().toInt()
                val max = s.edadMax.trim().toInt()
                if (idCategoria == Rutas.SIN_ID) {
                    repositorio.agregarCategoria(s.nombre.trim(), min, max)
                    _uiState.update { it.copy(guardando = false, guardadoCon = R.string.msg_categoria_agregada) }
                } else {
                    repositorio.actualizarCategoria(Categoria(idCategoria, s.nombre.trim(), min, max))
                    _uiState.update { it.copy(guardando = false, guardadoCon = R.string.msg_categoria_actualizada) }
                }
            } catch (e: ErrorDominio.NombreDuplicado) {
                // Vino de una SQLiteConstraintException (índice UNIQUE) convertida en excepción propia
                val msg = e.aMensajeUi()
                _uiState.update { it.copy(guardando = false, errorNombre = msg, mensaje = msg) }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, mensaje = e.aMensajeUi()) }
            }
        }
    }
}
