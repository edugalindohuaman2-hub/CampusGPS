package com.galindo.campusgps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.galindo.campusgps.data.LugaresRepository
import com.galindo.campusgps.data.UbicacionRepository
import com.galindo.campusgps.model.LugarConDistancia
import com.galindo.campusgps.model.Posicion
import com.galindo.campusgps.model.UbicacionUi
import com.galindo.campusgps.util.distanciaMetros
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapaViewModel(
    private val ubicacionRepository: UbicacionRepository,
    private val lugaresRepository: LugaresRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<UbicacionUi>(UbicacionUi.Inicial)
    val estado: StateFlow<UbicacionUi> = _estado.asStateFlow()

    private val _lugares = MutableStateFlow<List<LugarConDistancia>>(emptyList())
    val lugares: StateFlow<List<LugarConDistancia>> = _lugares.asStateFlow()

    private val _seleccionado = MutableStateFlow<Int?>(null)
    val seleccionado: StateFlow<Int?> = _seleccionado.asStateFlow()

    init {
        cargarLugaresIniciales()
    }

    private fun cargarLugaresIniciales() {
        val listaLugares = lugaresRepository.obtenerLugares()
        _lugares.value = listaLugares.map { LugarConDistancia(it, null) }
    }

    fun seleccionar(id: Int?) {
        _seleccionado.value = id
    }

    fun buscar() {
        _estado.value = UbicacionUi.Buscando
        viewModelScope.launch {
            val result = ubicacionRepository.obtenerPosicion()
            result.onSuccess { posicion ->
                _estado.value = UbicacionUi.Lista(posicion)
                actualizarDistancias(posicion)
            }.onFailure { exception ->
                _estado.value = UbicacionUi.Error(exception.message ?: "Error al obtener la ubicación.")
            }
        }
    }

    fun permisoDenegado() {
        _estado.value = UbicacionUi.Error("Permiso de ubicación denegado")
    }

    private fun actualizarDistancias(pos: Posicion) {
        val listaOrdenada = lugaresRepository.obtenerLugares().map { lugar ->
            LugarConDistancia(lugar, distanciaMetros(pos, lugar))
        }.sortedBy { it.metros }
        _lugares.value = listaOrdenada
    }
}
