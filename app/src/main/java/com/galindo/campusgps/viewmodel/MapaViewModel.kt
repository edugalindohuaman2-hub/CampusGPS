package com.galindo.campusgps.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.galindo.campusgps.data.LugaresRepository
import com.galindo.campusgps.data.UbicacionRepository
import com.galindo.campusgps.model.LugarConDistancia
import com.galindo.campusgps.model.Posicion
import com.galindo.campusgps.model.UbicacionUi
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

    private val _seleccionado = MutableStateFlow<LugarConDistancia?>(null)
    val seleccionado: StateFlow<LugarConDistancia?> = _seleccionado.asStateFlow()

    init {
        cargarLugaresIniciales()
    }

    private fun cargarLugaresIniciales() {
        val listaLugares = lugaresRepository.obtenerLugares()
        _lugares.value = listaLugares.map { LugarConDistancia(it, null) }
    }

    fun seleccionarLugar(lugar: LugarConDistancia?) {
        _seleccionado.value = lugar
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

    private fun actualizarDistancias(posicion: Posicion) {
        val listaLugares = lugaresRepository.obtenerLugares()
        _lugares.value = listaLugares.map { lugar ->
            val dist = calcularDistancia(posicion.latitud, posicion.longitud, lugar.latitud, lugar.longitud)
            LugarConDistancia(lugar, dist)
        }
    }

    private fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }
}
