package edu.cs371m.routenest.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.cs371m.routenest.data.model.ItineraryDay
import edu.cs371m.routenest.data.model.Trip
import edu.cs371m.routenest.presentation.ui.components.AutoCompleteField
import edu.cs371m.routenest.presentation.ui.components.DropDownSelection
import edu.cs371m.routenest.presentation.ui.components.Loading
import edu.cs371m.routenest.presentation.viewmodel.TripViewModel
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AddPlace(
    trip: Trip,
    snackBarHostState: SnackbarHostState,
    onNavToBack: () -> Unit,
    onTripModelAddPlace: (String, String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var placeName by remember { mutableStateOf("") }
    var selectedDay by remember { mutableStateOf<ItineraryDay?>(null) }
    var placeId by remember { mutableStateOf("") }
    val dateFormatter = remember {
        java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault())
    }

    Surface(
        modifier = Modifier.fillMaxSize()
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
                .padding(28.dp)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Add a Place",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp),
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )

                AutoCompleteField(
                    searchQuery = placeName,
                    textFieldText = "Add a place",
                    onQueryChange = {
                        placeName = it
                    },
                    onItemClick = {
                        placeName = it.getFullText(null).toString()
                        placeId = it.placeId
                    },
                    isPlace = true,
                    location = trip.title
                )

                DropDownSelection(
                    options = trip.days,
                    selectedItem = selectedDay,
                    onSelectedItem = { selected -> selectedDay = selected },
                    label = "Select a Day",
                    itemToText = { day -> dateFormatter.format(day.date.toDate()) },
                    leadingIcon = Icons.Default.DateRange
                )

                Button(
                    onClick = {
                        if (placeName.isNotBlank() && selectedDay != null) {
                            onTripModelAddPlace(placeId, selectedDay!!.id)
                        } else {
                            scope.launch {
                                snackBarHostState.showSnackbar(
                                    message = "Please enter a place name and select a day",
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Indefinite
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = placeName.isNotBlank()
                ) {
                    Text("Add Place")
                }
                Button(
                    onClick = {
                        onNavToBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}