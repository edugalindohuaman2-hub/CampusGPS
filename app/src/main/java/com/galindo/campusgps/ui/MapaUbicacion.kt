package com.galindo.campusgps.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.galindo.campusgps.model.LugarConDistancia
import com.galindo.campusgps.model.UbicacionUi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun MapaUbicacion(
    estado: UbicacionUi,
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-12.0464, -77.0428), 12f)
    }

    LaunchedEffect(estado) {
        if (estado is UbicacionUi.Lista) {
            val posicionLatLng = LatLng(estado.posicion.latitud, estado.posicion.longitud)
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(posicionLatLng, 16f)
            )
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState
    ) {
        lugares.forEach { lugarDist ->
            val esSeleccionado = lugarDist.lugar.id == seleccionado
            Marker(
                state = rememberUpdatedMarkerState(
                    position = LatLng(lugarDist.lugar.latitud, lugarDist.lugar.longitud)
                ),
                title = lugarDist.lugar.nombre + if (esSeleccionado) " (Seleccionado)" else "",
                snippet = lugarDist.lugar.categoria + (lugarDist.metros?.let { " - ${it.toInt()}m" } ?: "")
            )
        }

        if (estado is UbicacionUi.Lista) {
            Marker(
                state = rememberUpdatedMarkerState(
                    position = LatLng(estado.posicion.latitud, estado.posicion.longitud)
                ),
                title = "Estás aquí"
            )
        }
    }
}
