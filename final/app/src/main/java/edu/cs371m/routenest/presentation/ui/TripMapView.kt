package edu.cs371m.routenest.presentation.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import edu.cs371m.routenest.data.api.PlacesApi.getCityCoordinates
import edu.cs371m.routenest.data.model.ItineraryDay
import edu.cs371m.routenest.data.model.Place
import kotlinx.coroutines.launch

@Composable
fun TripMapView(
    dayToPlaceMapping: Map<ItineraryDay, List<Place>>,
    city: String,
    onNavBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState()
    val dateFormatter = remember {
        java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault())
    }
    val dayColorMap = remember(dayToPlaceMapping.keys) {
        dayToPlaceMapping.keys.mapIndexed { index, day ->
            day.id to (index.toFloat() * 360f / dayToPlaceMapping.keys.size.coerceAtLeast(1))
        }.toMap()
    }

    LaunchedEffect(Unit) {
        Log.d("TripMapView", "Day to place mapping: $dayToPlaceMapping")
        getCityCoordinates(city) {
            scope.launch {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(it, 12f),
                    1000
                )
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onNavBack() }
                    ) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Home")
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxWidth(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        scrollGesturesEnabled = true,
                        rotationGesturesEnabled = true,
                        tiltGesturesEnabled = false
                    )
                ) {
                    dayToPlaceMapping.forEach { (day, places) ->
                        val color = dayColorMap[day.id] ?: BitmapDescriptorFactory.HUE_RED

                        places.forEach { place ->
                            Marker(
                                state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                                title = place.name,
                                snippet = dateFormatter.format(day.date.toDate()),
                                icon = BitmapDescriptorFactory.defaultMarker(color)
                            )
                        }
                    }
                }
                Card(
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                ) {
                    Column {
                        Text(
                            text = "Days Legend",
                            style = MaterialTheme.typography.titleMedium
                        )

                        dayToPlaceMapping.keys.forEach { day ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            color = Color.hsv(dayColorMap[day.id] ?: 0f, 1f, 1f),
                                            shape = CircleShape
                                        ),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = dateFormatter.format(day.date.toDate()),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}