package com.galindo.campusgps.data

import com.galindo.campusgps.model.Lugar

interface LugaresRepository {
    fun obtenerLugares(): List<Lugar>
}
