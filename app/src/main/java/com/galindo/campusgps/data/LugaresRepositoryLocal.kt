package com.galindo.campusgps.data

import com.galindo.campusgps.model.Lugar

class LugaresRepositoryLocal : LugaresRepository {
    override fun obtenerLugares(): List<Lugar> {
        return listOf(
            Lugar(1, "Biblioteca Tecsup", "Estudio", -12.0427, -76.9527),
            Lugar(2, "Cafetería Tecsup", "Comida", -12.0435, -76.9528),
            Lugar(3, "Cancha de deporte", "Deporte", -12.0429, -76.9533),
            Lugar(4, "Auditorio Tecsup", "Eventos", -12.0438, -76.9535),
            Lugar(5, "Estacionamiento Tecsup", "Servicios", -12.0440, -76.9530)
        )
    }
}
