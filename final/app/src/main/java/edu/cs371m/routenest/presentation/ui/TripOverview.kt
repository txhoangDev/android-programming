package edu.cs371m.routenest.presentation.ui

import android.content.ClipData
import android.content.ClipDescription
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import edu.cs371m.routenest.data.model.ItineraryDay
import edu.cs371m.routenest.data.model.Place
import edu.cs371m.routenest.data.model.Trip
import edu.cs371m.routenest.presentation.ui.components.CollapsibleSection
import edu.cs371m.routenest.presentation.ui.components.PlaceDetails
import edu.cs371m.routenest.presentation.ui.components.SwipeDelete
import java.text.SimpleDateFormat
import java.util.Locale

private data class PlaceDragData(
    val placeId: String,
    val fromDayId: String,
)

@Composable
private fun CreateTextField(content: String, onContentChange: (String) -> Unit) {
    return TextField(
        value = content,
        onValueChange = { onContentChange(it) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CreateListViewForPlace(places: List<Place>) {
    Log.d("CreateListViewForPlace", "places: $places")
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(
            items = places,
            key = { item -> item.id }
        ) { place ->
            PlaceDetails(
                place = place
            )
        }
    }
}

@Composable
private fun TripOverviewTabContent(notes: String, onNotesChange: (String) -> Unit, places: List<Place>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        CollapsibleSection(
            content = { CreateTextField(content = notes, onContentChange = { onNotesChange(it) }) },
            title = "Notes"
        )

        CollapsibleSection(
            content = { CreateListViewForPlace(places) },
            title = "Places to Visit"
        )
    }
}

@Composable
private fun TripItineraryTabContent(
    dayToPlaceMapping: Map<ItineraryDay, List<Place>>,
    onLocalMove: (Map<ItineraryDay, List<Place>>) -> Unit,
) {
    var expandedStates by remember { mutableStateOf(setOf<String>()) }
    var dragOverDayId by remember { mutableStateOf<String?>(null) }
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        dayToPlaceMapping.forEach { (day, places) ->
            val isExpanded = expandedStates.contains(day.id)

            item(key = day.id) {
                val dropTarget = remember(day.id) {
                    object : DragAndDropTarget {
                        override fun onEntered(event: DragAndDropEvent) {
                            dragOverDayId = day.id
                        }

                        override fun onExited(event: DragAndDropEvent) {
                            if (dragOverDayId == day.id) dragOverDayId = null
                        }

                        override fun onEnded(event: DragAndDropEvent) {
                            if (dragOverDayId == day.id) dragOverDayId = null
                        }

                        override fun onDrop(event: DragAndDropEvent): Boolean {
                            val dragData = event.toAndroidDragEvent().localState as? PlaceDragData
                            val placeId = dragData?.placeId
                            val fromDayId = dragData?.fromDayId

                            if (placeId != null && fromDayId != null && fromDayId != day.id) {
                                val placeToMove = dayToPlaceMapping.values.flatten().find { it.id == placeId }
                                val updatedMap = dayToPlaceMapping.toMutableMap()
                                if (placeToMove != null) {
                                    val oldDay = updatedMap.keys.find { it.id == fromDayId }
                                    if (oldDay != null) {
                                        updatedMap[oldDay] = updatedMap[oldDay]!!.filter { it.id != placeId }
                                    }

                                    updatedMap[day] = updatedMap[day]?.plus(placeToMove) ?: listOf(placeToMove)
                                }
                                onLocalMove(updatedMap)
                                dragOverDayId = null
                                return true
                            }

                            return false
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (dragOverDayId == day.id) {
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                            } else {
                                Color.Transparent
                            }
                        )
                        .dragAndDropTarget(
                            shouldStartDragAndDrop = { startEvent ->
                                startEvent.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                            },
                            target = dropTarget,
                        )
                        .clickable {
                            expandedStates = if (isExpanded) {
                                expandedStates - day.id
                            } else {
                                expandedStates + day.id
                            }
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.rotate(if (isExpanded) 0f else 270f)
                    )
                    Text(text = formatter.format(day.date.toDate()))
                }
            }

            if (isExpanded) {
                if (places.isNotEmpty()) {
                    items(
                        items = places,
                        key = { place -> "${day.id}-${place.id}" }
                    ) { place ->
                        SwipeDelete(
                            onDelete = {
                                val updatedMap = dayToPlaceMapping.toMutableMap()
                                updatedMap[day] = updatedMap[day]!!.filter { it.id != place.id }
                                onLocalMove(updatedMap)
                            },
                            content = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .dragAndDropSource(transferData = { _ ->
                                            DragAndDropTransferData(
                                                clipData = ClipData.newPlainText(
                                                    "place",
                                                    place.id,
                                                ),
                                                localState = PlaceDragData(
                                                    placeId = place.id,
                                                    fromDayId = day.id,
                                                )
                                            )
                                        })
                                ) {
                                    PlaceDetails(place = place)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TripOverview(
    trip: Trip,
    snackBarHostState: SnackbarHostState,
    dayToPlaceMapping: Map<ItineraryDay, List<Place>>,
    onTripModelUpdateTrip: (String, Map<ItineraryDay, List<Place>>, String) -> Unit,
) {
    val tabs = listOf("Overview", "Itinerary")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var notes by remember { mutableStateOf(trip.notes) }
    var places by remember { mutableStateOf(trip.places) }
    var localItinerary by remember(dayToPlaceMapping) {
        mutableStateOf(dayToPlaceMapping)
    }

    Surface(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackBarHostState)  { data ->
                    Snackbar(
                        containerColor = Color(0xFFF44336),
                        snackbarData = data
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(12.dp),
            topBar = {
                Column {
                    CenterAlignedTopAppBar(
                        title = {
                            Text("Trip to ${trip.title}", style = MaterialTheme.typography.titleLarge)
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { onTripModelUpdateTrip(notes, localItinerary, "home") }
                            ) {
                                Icon(Icons.Default.Home, contentDescription = "Home")
                            }
                        },
                        actions = {},
                        modifier = Modifier.background(MaterialTheme.colorScheme.background)
                    )

                    SecondaryTabRow(
                        selectedTabIndex = selectedTabIndex,
                        modifier = Modifier.background(MaterialTheme.colorScheme.background)
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                text = { Text(text = title, style = MaterialTheme.typography.titleMedium) },
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    FloatingActionButton(
                        onClick = {
                            Log.d("TripOverview", "${localItinerary}")
                            onTripModelUpdateTrip(notes, localItinerary, "addPlace")
                        },
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add a place"
                        )
                    }
                    Spacer(modifier = Modifier.padding(8.dp))
                    FloatingActionButton(
                        onClick = {
                            onTripModelUpdateTrip(notes, localItinerary, "mapView")
                        },
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = "Map View of trip"
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier.padding(innerPadding)
            ) {
                when (selectedTabIndex) {
                    0 -> TripOverviewTabContent(notes, { notes = it }, places)
                    1 ->
                        TripItineraryTabContent(
                            dayToPlaceMapping = localItinerary,
                            onLocalMove = {
                                localItinerary = it
                                places = it.values.flatten()
                            }
                        )
                }
            }
        }
    }
}
