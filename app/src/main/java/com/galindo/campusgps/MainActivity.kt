package com.galindo.campusgps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.galindo.campusgps.data.LugaresRepositoryLocal
import com.galindo.campusgps.data.UbicacionRepositoryGoogle
import com.galindo.campusgps.ui.PantallaMapa
import com.galindo.campusgps.ui.theme.CampusGPSTheme
import com.galindo.campusgps.viewmodel.CampusViewModelFactory
import com.galindo.campusgps.viewmodel.MapaViewModel

class MainActivity : ComponentActivity() {

    private val mapaViewModel: MapaViewModel by viewModels {
        CampusViewModelFactory(
            ubicacionRepository = UbicacionRepositoryGoogle(this),
            lugaresRepository = LugaresRepositoryLocal()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusGPSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PantallaMapa(viewModel = mapaViewModel)
                }
            }
        }
    }
}
