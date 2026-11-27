package com.julio.carrerasatletismo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julio.carrerasatletismo.R
import com.julio.carrerasatletismo.domain.model.Categoria
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

data class CategoriasUiState(
    val cargando: Boolean = true,
    val categorias: List<Categoria> = emptyList(),
    val error: MensajeUi? = null,
    val mensaje: MensajeUi? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoriasViewModel @Inject constructor(
    private val repositorio: CarrerasRepository
) : ViewModel() {

    private val reintentos = MutableStateFlow(0)
    private val mensaje = MutableStateFlow<MensajeUi?>(null)

    private val lista = reintentos.flatMapLatest {
        repositorio.observarCategorias()
            .map<List<Categoria>, EstadoLista<Categoria>> { EstadoLista.Listo(it) }
            .catch { e -> emit(EstadoLista.Error(e.aMensajeUi())) }
    }

    val uiState: StateFlow<CategoriasUiState> =
        combine(lista, mensaje) { estado, msg ->
            CategoriasUiState(
                cargando = estado is EstadoLista.Cargando,
                categorias = if (estado is EstadoLista.Listo<Categoria>) estado.datos else emptyList(),
                error = if (estado is EstadoLista.Error) estado.mensaje else null,
                mensaje = msg
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoriasUiState())

    fun onReintentar() = reintentos.update { it + 1 }

    fun onEliminar(idCategoria: Long) {
        viewModelScope.launch {
            try {
                repositorio.eliminarCategoria(idCategoria)
                mensaje.value = MensajeUi(R.string.msg_categoria_eliminada)
            } catch (e: Exception) {
                // Si tiene corredores llega ErrorDominio.CategoriaEnUso → mensaje personalizado
                mensaje.value = e.aMensajeUi()
            }
        }
    }

    fun onMensajeMostrado() {
        mensaje.value = null
    }
}
