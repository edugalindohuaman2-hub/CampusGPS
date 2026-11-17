package com.galindo.campusgps.util

import android.location.Location
import com.galindo.campusgps.model.Lugar
import com.galindo.campusgps.model.Posicion

fun distanciaMetros(desde: Posicion, hasta: Lugar): Float {
    val results = FloatArray(1)
    Location.distanceBetween(
        desde.latitud,
        desde.longitud,
        hasta.latitud,
        hasta.longitud,
        results
    )
    return results[0]
}
