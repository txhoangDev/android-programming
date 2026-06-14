package edu.cs371m.routenest.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.AndroidEntryPoint
import edu.cs371m.routenest.R
import edu.cs371m.routenest.presentation.ui.navigation.NavigationStack
import edu.cs371m.routenest.presentation.ui.theme.RouteNestTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // create Places API
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(applicationContext, getString(R.string.google_maps_key))
        }

        setContent {
            RouteNestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    NavigationStack()
                }
            }
        }
    }
}