package edu.cs371m.routenest.presentation.ui.navigation

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.cs371m.routenest.presentation.ui.AddPlace
import edu.cs371m.routenest.presentation.viewmodel.AuthViewModel
import edu.cs371m.routenest.presentation.viewmodel.TripViewModel
import edu.cs371m.routenest.presentation.ui.AddTrip
import edu.cs371m.routenest.presentation.ui.Home
import edu.cs371m.routenest.presentation.ui.Login
import edu.cs371m.routenest.presentation.ui.SignUp
import edu.cs371m.routenest.presentation.ui.TripMapView
import edu.cs371m.routenest.presentation.ui.TripOverview
import edu.cs371m.routenest.presentation.ui.WelcomeScreen
import edu.cs371m.routenest.presentation.ui.components.Loading
import edu.cs371m.routenest.presentation.ui.theme.RouteNestTheme

// routes
sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object AddTrip : Screen("addTrip")
    object TripOverview : Screen("tripOverview")
    object AddPlace : Screen("addPlace")
    object MapView : Screen("mapView")
}

@Composable
fun NavigationStack() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val tripViewModel: TripViewModel = hiltViewModel()
    val currentTrip by tripViewModel.trip.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackBarHostState = remember { SnackbarHostState() }
    val currentUser by authViewModel.user

    // create nav effect upon successful login/signup
    LaunchedEffect(currentUser) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        Log.d("NavigationStack", "Current route: $currentRoute")
        Log.d("NavigationStack", "User: $currentUser")
        Log.d("NavigationStack", "Loading: ${currentUser?.displayName}")
        if (currentUser != null) {
            tripViewModel.getListOfTrips()
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Welcome.route) { inclusive = true }
            }
        } else if (currentRoute != Screen.Welcome.route &&
                currentRoute != Screen.Login.route &&
                currentRoute != Screen.SignUp.route) {
            navController.navigate(Screen.Welcome.route) {
                popUpTo(0)
            }
        }
    }

    // Navigate to trip overview once trip retrieval is success
    LaunchedEffect(currentTrip) {
        Log.d("NavigationStack", "Current trip: $currentTrip")
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        if (currentTrip != null) {
            if (currentRoute == Screen.Home.route) {
                navController.navigate(Screen.TripOverview.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }
        }
    }

    RouteNestTheme {
        Scaffold() { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                NavHost(navController = navController, startDestination = Screen.Welcome.route) {
                    composable(route = Screen.Welcome.route) {
                        WelcomeScreen(
                            onSignUpNav = {
                                navController.navigate(Screen.SignUp.route)
                            },
                            onLoginNav = {
                                navController.navigate(Screen.Login.route)
                            }
                        )
                    }

                    composable(route = Screen.Login.route) {
                        val authLoading = authViewModel.loading.value

                        LaunchedEffect(Unit) {
                            authViewModel.errors.collect { error ->
                                snackBarHostState.showSnackbar(
                                    message = error,
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Indefinite
                                )
                            }
                        }

                        Login(
                            snackBarHostState = snackBarHostState,
                            onLogin = { email, password ->
                                authViewModel.login(email, password)
                            },
                            onNavToSignUp = {
                                navController.navigate(Screen.SignUp.route)
                            }
                        )

                        AnimatedVisibility(
                            visible = authLoading,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Loading()
                        }
                    }

                    composable(route = Screen.SignUp.route) {
                        val authLoading = authViewModel.loading.value

                        LaunchedEffect(Unit) {
                            authViewModel.errors.collect { error ->
                                snackBarHostState.showSnackbar(
                                    message = error,
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Indefinite
                                )
                            }
                        }

                        SignUp(
                            snackBarHostState = snackBarHostState,
                            onSignUp = { email, password, name ->
                                authViewModel.createAccount(name, email, password)
                            },
                            onNavToLogIn = {
                                navController.navigate(Screen.Login.route)
                            }
                        )

                        AnimatedVisibility(
                            visible = authLoading,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Loading()
                        }
                    }

                    composable(route = Screen.Home.route) {
                        val tripLoading = tripViewModel.loading.value

                        Home(
                            name = currentUser?.displayName ?: "User",
                            trips = tripViewModel.trips.value,
                            onTripClick = {
                                Log.d("NavigationStack", "Trip clicked: ${it.id}")
                                tripViewModel.getTripById(it.id)
                            },
                            onLogout = {
                                tripViewModel.userLogOut()
                                authViewModel.logout()
                            },
                            onAddTripNav = {
                                navController.navigate(Screen.AddTrip.route)
                            },
                            onDeleteTrip = { tripId ->
                                tripViewModel.deleteTrip(tripId)
                            }
                        )

                        AnimatedVisibility(
                            visible = tripLoading,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Loading()
                        }
                    }

                    composable(route = Screen.AddTrip.route) {
                        val loading by tripViewModel.loading

                        LaunchedEffect(Unit) {
                            tripViewModel.errors.collect { error ->
                                snackBarHostState.showSnackbar(
                                    message = error,
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Indefinite
                                )
                            }
                        }

                        LaunchedEffect(Unit) {
                            tripViewModel.navDestination.collect { route ->
                                navController.navigate(route)
                            }
                        }

                        AddTrip(
                            snackBarHostState = snackBarHostState,
                            onNavToHome = {
                                navController.navigate(Screen.Home.route)
                            },
                            onTripModelCreateTrip = { title, destinationId, start, end ->
                                tripViewModel.createTrip(title, destinationId, start, end, Screen.Home.route)
                            }
                        )

                        AnimatedVisibility(
                            visible = loading,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Loading()
                        }
                    }

                    composable(route = Screen.TripOverview.route) {
                        if (currentTrip == null) {
                            Loading()
                        } else {
                            val trip = currentTrip!!
                            val mapping = remember() { tripViewModel.getDayToPlaceMapping() }

                            LaunchedEffect(Unit) {
                                tripViewModel.errors.collect { error ->
                                    snackBarHostState.showSnackbar(
                                        message = error,
                                        withDismissAction = true,
                                        duration = SnackbarDuration.Indefinite
                                    )
                                }
                            }

                            LaunchedEffect(Unit) {
                                tripViewModel.navDestination.collect { route ->
                                    when (route) {
                                        Screen.Home.route -> {
                                            tripViewModel.resetTrip()
                                            navController.navigate(Screen.Home.route)
                                        }
                                        Screen.MapView.route -> {
                                            navController.navigate(Screen.MapView.route)
                                        }
                                        Screen.AddPlace.route -> {
                                            navController.navigate(Screen.AddPlace.route)
                                        }
                                    }
                                }
                            }

                            TripOverview(
                                trip = trip,
                                snackBarHostState = snackBarHostState,
                                dayToPlaceMapping = mapping,
                                onTripModelUpdateTrip = { notes, updatedItinerary, nextRoute ->
                                    val places = updatedItinerary.values.flatten()
                                    val updatedDays = updatedItinerary.map { (day, places) ->
                                        day.copy(placeIds = places.map { it.id })
                                    }
                                    val trip = trip.copy(notes = notes, days = updatedDays, places = places)
                                    Log.d("NavigationStack", "Updated trip: $trip")
                                    tripViewModel.updateTrip(trip, nextRoute)
                                },
                            )
                        }
                    }

                    composable(route = Screen.AddPlace.route) {
                        val trip = currentTrip!!

                        LaunchedEffect(Unit) {
                            tripViewModel.errors.collect { error ->
                                snackBarHostState.showSnackbar(
                                    message = error,
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Indefinite
                                )
                            }
                        }

                        LaunchedEffect(Unit) {
                            tripViewModel.navDestination.collect { route ->
                                navController.navigateUp()
                            }
                        }

                        AddPlace(
                            trip = trip,
                            snackBarHostState = snackBarHostState,
                            onNavToBack = {
                                navController.navigateUp()
                            },
                            onTripModelAddPlace = { placeId, selectedDayId ->
                                tripViewModel.addPlaceToTrip(trip.id, placeId, selectedDayId, Screen.TripOverview.route)
                            }
                        )
                    }

                    composable(route = Screen.MapView.route) {
                        val trip = currentTrip!!
                        TripMapView(
                            dayToPlaceMapping = tripViewModel.getDayToPlaceMapping(),
                            city = trip.title,
                            onNavBack = {
                                navController.navigateUp()
                            }
                        )
                    }
                }
            }
        }
    }
}
