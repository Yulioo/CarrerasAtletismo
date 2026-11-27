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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CorredorFormUiState(
    val esEdicion: Boolean = false,
    val cargando: Boolean = false,
    val nombre: String = "",
    val edad: String = "",
    /** Nombre de la categoría que se asignará según la edad escrita. */
    val categoriaSugerida: String? = null,
    val errorNombre: MensajeUi? = null,
    val errorEdad: MensajeUi? = null,
    val guardando: Boolean = false,
    val mensaje: MensajeUi? = null,
    @StringRes val guardadoCon: Int? = null
)

@HiltViewModel
class CorredorFormViewModel @Inject constructor(
    private val repositorio: CarrerasRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /** Ruta "formulario_corredor?id={id}" con defaultValue = -1 → -1 significa corredor nuevo. */
    private val idCorredor: Long = savedStateHandle.get<Long>(Rutas.ARG_ID) ?: Rutas.SIN_ID
    private var categorias: List<Categoria> = emptyList()

    private val _uiState = MutableStateFlow(CorredorFormUiState(esEdicion = idCorredor != Rutas.SIN_ID))
    val uiState: StateFlow<CorredorFormUiState> = _uiState.asStateFlow()

    init {
        // Observer: si cambian las categorías, la sugerencia se recalcula
        viewModelScope.launch {
            repositorio.observarCategorias()
                .catch { /* si falla, simplemente no se muestra la sugerencia */ }
                .collect { lista ->
                    categorias = lista
                    _uiState.update { it.copy(categoriaSugerida = sugerirCategoria(it.edad)) }
                }
        }
        if (idCorredor != Rutas.SIN_ID) {
            viewModelScope.launch {
                _uiState.update { it.copy(cargando = true) }
                try {
                    val corredor = repositorio.obtenerCorredor(idCorredor) ?: throw ErrorDominio.NoEncontrado()
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            nombre = corredor.nombre,
                            edad = corredor.edad.toString(),
                            categoriaSugerida = sugerirCategoria(corredor.edad.toString())
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(cargando = false, mensaje = e.aMensajeUi()) }
                }
            }
        }
    }

    private fun sugerirCategoria(edadTexto: String): String? {
        val edad = edadTexto.toIntOrNull() ?: return null
        return categorias.filter { edad in it.edadMin..it.edadMax }.minByOrNull { it.edadMin }?.nombre
    }

    // ---------------- Eventos (UDF) ----------------
    fun onNombreChange(valor: String) = _uiState.update { it.copy(nombre = valor.take(60), errorNombre = null) }

    fun onEdadChange(valor: String) {
        val limpio = valor.filter(Char::isDigit).take(3) // formato numérico
        _uiState.update { it.copy(edad = limpio, errorEdad = null, categoriaSugerida = sugerirCategoria(limpio)) }
    }

    fun onMensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    fun onGuardar() {
        val s = _uiState.value
        if (s.guardando) return
        val errorNombre = Validaciones.nombrePersona(s.nombre)
        val errorEdad = Validaciones.entero(s.edad, 1, 120)
        if (errorNombre != null || errorEdad != null) {
            _uiState.update {
                it.copy(errorNombre = errorNombre, errorEdad = errorEdad, mensaje = MensajeUi(R.string.error_revisa_campos))
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            try {
                val edad = s.edad.trim().toInt()
                if (idCorredor == Rutas.SIN_ID) {
                    repositorio.agregarCorredor(s.nombre.trim(), edad)
                    _uiState.update { it.copy(guardando = false, guardadoCon = R.string.msg_corredor_agregado) }
                } else {
                    repositorio.actualizarCorredor(idCorredor, s.nombre.trim(), edad)
                    _uiState.update { it.copy(guardando = false, guardadoCon = R.string.msg_corredor_actualizado) }
                }
            } catch (e: ErrorDominio.NombreDuplicado) {
                val msg = e.aMensajeUi()
                _uiState.update { it.copy(guardando = false, errorNombre = msg, mensaje = msg) }
            } catch (e: ErrorDominio.SinCategoriaParaEdad) {
                val msg = e.aMensajeUi()
                _uiState.update { it.copy(guardando = false, errorEdad = msg, mensaje = msg) }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, mensaje = e.aMensajeUi()) }
            }
        }
    }
}
