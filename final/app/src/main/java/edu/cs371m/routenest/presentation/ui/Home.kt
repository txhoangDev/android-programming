package edu.cs371m.routenest.presentation.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import edu.cs371m.routenest.R
import edu.cs371m.routenest.data.model.TripSummary
import edu.cs371m.routenest.presentation.ui.components.SwipeDelete
import edu.cs371m.routenest.presentation.ui.components.TripDetails

@Composable
fun Home(
    name: String,
    trips: List<TripSummary>,
    onTripClick: (TripSummary) -> Unit = {},
    onLogout: () -> Unit = {},
    onAddTripNav: () -> Unit = {},
    onDeleteTrip: (String) -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(28.dp),
            floatingActionButton = {
                SmallFloatingActionButton(
                    onClick = {
                        Log.d("Home", "add trip clicked")
                        onAddTripNav()
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_trip)
                    )
                }
            },
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Welcome back, ${name}!",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    IconButton(
                        onClick = { onLogout() }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_logout_24),
                            contentDescription = stringResource(R.string.add_trip)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(
                        items = trips,
                        key = { trip -> trip.id }
                    ) { trip ->
                        SwipeDelete(
                            onDelete = { onDeleteTrip(trip.id) },
                            content = {
                                TripDetails(
                                    trip = trip,
                                    onClick = { onTripClick(trip) }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
