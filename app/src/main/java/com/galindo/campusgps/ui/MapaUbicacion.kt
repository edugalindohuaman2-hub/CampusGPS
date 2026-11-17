package com.galindo.campusgps.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.galindo.campusgps.model.Lugar
import com.galindo.campusgps.model.LugarConDistancia
import com.galindo.campusgps.model.UbicacionUi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

@Composable
fun MapaUbicacion(
    estado: UbicacionUi,
    lugares: List<LugarConDistancia>,
    seleccionado: Int?,
    masCercano: Lugar?,
    modifier: Modifier = Modifier,
    tienePermiso: Boolean = false
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-12.0432, -76.9531), 16.5f)
    }

    LaunchedEffect(estado) {
        if (estado is UbicacionUi.Lista) {
            val posicionLatLng = LatLng(estado.posicion.latitud, estado.posicion.longitud)
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(posicionLatLng, 16f)
            )
        }
    }

    LaunchedEffect(seleccionado) {
        if (seleccionado != null) {
            val lugarSel = lugares.firstOrNull { it.lugar.id == seleccionado }
            if (lugarSel != null) {
                val coords = LatLng(lugarSel.lugar.latitud, lugarSel.lugar.longitud)
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(coords, 17f)
                )
            }
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = tienePermiso),
        uiSettings = MapUiSettings(myLocationButtonEnabled = tienePermiso)
    ) {
        lugares.forEach { item ->
            key(item.lugar.id) {
                val esMasCercano = item.lugar.id == masCercano?.id
                Marker(
                    state = rememberUpdatedMarkerState(
                        position = LatLng(item.lugar.latitud, item.lugar.longitud)
                    ),
                    title = item.lugar.nombre,
                    snippet = item.lugar.categoria,
                    icon = BitmapDescriptorFactory.defaultMarker(
                        if (esMasCercano) BitmapDescriptorFactory.HUE_GREEN
                        else BitmapDescriptorFactory.HUE_AZURE
                    )
                )
            }
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
