package com.galindo.campusgps.data

import android.annotation.SuppressLint
import android.content.Context
import com.galindo.campusgps.model.Posicion
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

class UbicacionRepositoryGoogle(
    private val fusedLocationClient: FusedLocationProviderClient
) : UbicacionRepository {

    constructor(context: Context) : this(LocationServices.getFusedLocationProviderClient(context))

    @SuppressLint("MissingPermission")
    override suspend fun obtenerPosicion(): Result<Posicion> {
        return try {
            val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            if (location != null) {
                Result.success(Posicion(location.latitude, location.longitude))
            } else {
                Result.failure(Exception("No se pudo obtener la ubicación actual."))
            }
        } catch (e: SecurityException) {
            Result.failure(Exception("Permiso de ubicación no otorgado."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
