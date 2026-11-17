package com.galindo.campusgps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.galindo.campusgps.data.LugaresRepository
import com.galindo.campusgps.data.UbicacionRepository
import com.galindo.campusgps.model.Lugar
import com.galindo.campusgps.model.LugarConDistancia
import com.galindo.campusgps.model.Posicion
import com.galindo.campusgps.model.TipoError
import com.galindo.campusgps.model.UbicacionUi
import com.galindo.campusgps.util.distanciaMetros
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapaViewModel(
    private val ubicacionRepository: UbicacionRepository,
    private val lugaresRepository: LugaresRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UbicacionUi>(UbicacionUi.Inicial)
    val estado: StateFlow<UbicacionUi> = _estado.asStateFlow()

    private val _lugares = MutableStateFlow<List<LugarConDistancia>>(emptyList())
    val lugares: StateFlow<List<LugarConDistancia>> = _lugares.asStateFlow()

    private val _categoriaSeleccionada = MutableStateFlow<String?>(null)
    val categoriaSeleccionada: StateFlow<String?> = _categoriaSeleccionada.asStateFlow()

    private val _radioMetros = MutableStateFlow<Float>(200f)
    val radioMetros: StateFlow<Float> = _radioMetros.asStateFlow()

    val lugaresFiltrados: StateFlow<List<LugarConDistancia>> = combine(lugares, categoriaSeleccionada) { lista, cat ->
        if (cat == null || cat == "Todas") {
            lista
        } else {
            lista.filter { it.lugar.categoria.equals(cat, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _seleccionado = MutableStateFlow<Int?>(null)
    val seleccionado: StateFlow<Int?> = _seleccionado.asStateFlow()

    val masCercano: StateFlow<Lugar?> = lugares
        .map { lista -> lista.firstOrNull { it.metros != null }?.lugar }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        cargarLugaresIniciales()
    }

    private fun cargarLugaresIniciales() {
        val listaLugares = lugaresRepository.obtenerLugares()
        _lugares.value = listaLugares.map { LugarConDistancia(it, null) }
    }

    fun cambiarRadio(metros: Float) {
        _radioMetros.value = metros
    }

    fun filtrarPorCategoria(categoria: String?) {
        _categoriaSeleccionada.value = if (categoria == "Todas") null else categoria
    }

    fun seleccionar(id: Int?) {
        _seleccionado.value = id
    }

    fun buscar() {
        if (!ubicacionRepository.playServicesDisponible()) {
            _estado.value = UbicacionUi.Error(
                mensaje = "Este dispositivo no tiene Google Play Services",
                tipo = TipoError.SIN_PLAY_SERVICES
            )
            return
        }

        _estado.value = UbicacionUi.Buscando
        viewModelScope.launch {
            val result = ubicacionRepository.obtenerPosicion()
            result.onSuccess { posicion ->
                _estado.value = UbicacionUi.Lista(posicion)
                actualizarDistancias(posicion)
            }.onFailure { exception ->
                _estado.value = mensajeDe(exception)
            }
        }
    }

    fun permisoDenegado() {
        _estado.value = UbicacionUi.Error(
            mensaje = "Permiso de ubicación denegado",
            tipo = TipoError.SIN_PERMISO
        )
    }

    private fun mensajeDe(e: Throwable): UbicacionUi.Error {
        val msg = e.message ?: ""
        return if (msg.contains("GPS", ignoreCase = true) || msg.contains("apagado", ignoreCase = true)) {
            UbicacionUi.Error(
                mensaje = "No se pudo obtener la ubicación. Activa el GPS e intenta de nuevo",
                tipo = TipoError.GPS_APAGADO
            )
        } else {
            UbicacionUi.Error(
                mensaje = e.message ?: "Error al obtener la ubicación.",
                tipo = TipoError.OTRO
            )
        }
    }

    private fun actualizarDistancias(pos: Posicion) {
        val listaOrdenada = lugaresRepository.obtenerLugares().map { lugar ->
            LugarConDistancia(lugar, distanciaMetros(pos, lugar))
        }.sortedBy { it.metros }
        _lugares.value = listaOrdenada
    }
}
