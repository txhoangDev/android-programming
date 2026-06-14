package edu.cs371m.routenest.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import edu.cs371m.routenest.presentation.ui.components.AutoCompleteField
import edu.cs371m.routenest.presentation.ui.components.Loading
import edu.cs371m.routenest.presentation.ui.navigation.Screen
import edu.cs371m.routenest.presentation.viewmodel.TripViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private fun utcEpochMillisToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

private fun LocalDate.toUtcEpochMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AddTrip(
    snackBarHostState: SnackbarHostState,
    onNavToHome: () -> Unit = {},
    onTripModelCreateTrip: (String, String, LocalDate, LocalDate) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    var title by remember { mutableStateOf("") }
    var destinationId by remember { mutableStateOf("") }
    var showStartSelector by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var showEndSelector by remember { mutableStateOf(false) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    val startPickerState = rememberDatePickerState()
    val endPickerState = rememberDatePickerState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
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
                    text = "Plan a Trip",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp),
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )

                AutoCompleteField(
                    searchQuery = title,
                    textFieldText = "Where to?",
                    onQueryChange = { title = it },
                    onItemClick = {
                        title = it.getFullText(null).toString()
                        destinationId = it.placeId
                    },
                    isPlace = false
                )

                Box {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = startDate?.format(formatter).orEmpty(),
                        onValueChange = {},
                        label = { Text("Start Date") },
                        readOnly = true,
                        leadingIcon = {
                            IconButton(
                                onClick = {
                                    startPickerState.selectedDateMillis = startDate?.toUtcEpochMillis()
                                    showStartSelector = true
                                }
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = "")
                            }
                        }
                    )

                    Box(
                        modifier = Modifier.matchParentSize().clickable {
                            startPickerState.selectedDateMillis = startDate?.toUtcEpochMillis()
                            showStartSelector = true
                        }
                    )
                }

                if (showStartSelector) {
                    DatePickerDialog(
                        onDismissRequest = { showStartSelector = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (startPickerState.selectedDateMillis == null) return@TextButton
                                    startPickerState.selectedDateMillis
                                        ?.let { millis ->
                                            startDate = utcEpochMillisToLocalDate(millis)
                                        }
                                    showStartSelector = false
                                }
                            ) {
                                Text("OK")
                            }
                        }
                    ) {
                        DatePicker(state = startPickerState)
                    }
                }

                Box {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = endDate?.format(formatter).orEmpty(),
                        onValueChange = {},
                        label = { Text("End Date") },
                        readOnly = true,
                        leadingIcon = {
                            IconButton(
                                onClick = {
                                    endPickerState.selectedDateMillis = endDate?.toUtcEpochMillis()
                                    showEndSelector = true
                                }
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = "")
                            }
                        }
                    )

                    Box(
                        modifier = Modifier.matchParentSize().clickable {
                            endPickerState.selectedDateMillis = endDate?.toUtcEpochMillis()
                            showEndSelector = true
                        }
                    )
                }

                if (showEndSelector) {
                    DatePickerDialog(
                        onDismissRequest = { showEndSelector = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (endPickerState.selectedDateMillis == null) return@TextButton
                                    endPickerState.selectedDateMillis
                                        ?.let { millis ->
                                            endDate = utcEpochMillisToLocalDate(millis)
                                        }
                                    showEndSelector = false
                                }
                            ) {
                                Text("OK")
                            }
                        }
                    ) {
                        DatePicker(state = endPickerState)
                    }
                }

                Button(
                    onClick = {
                        if (title.isBlank() || startDate == null || endDate == null) {
                            scope.launch {
                                snackBarHostState.showSnackbar(
                                    message = "Please enter a title and select a start and end date",
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Indefinite
                                )
                            }
                        } else {
                            val start = startDate!!
                            val end = endDate!!
                            onTripModelCreateTrip(title, destinationId, start, end)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotBlank() && startDate != null && endDate != null
                ) {
                    Text("Add Trip")
                }
                Button(
                    onClick = {
                        onNavToHome()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
