package edu.cs371m.routenest.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.cs371m.routenest.data.api.PlacesApi
import edu.cs371m.routenest.data.model.ItineraryDay
import edu.cs371m.routenest.data.model.Place
import edu.cs371m.routenest.data.model.Trip
import edu.cs371m.routenest.data.model.TripSummary
import edu.cs371m.routenest.data.repository.TripRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
    private val tripRepository: TripRepository
): ViewModel() {
    private val _trips = mutableStateOf<List<TripSummary>>(emptyList())
    val trips: State<List<TripSummary>> = _trips

    private val _trip = MutableStateFlow<Trip?>(null)
    val trip = _trip.asStateFlow()

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _errors = Channel<String>()
    val errors = _errors.receiveAsFlow()

    private val _navDestination = Channel<String>()
    val navDestination = _navDestination.receiveAsFlow()

    private fun convertLocalDate(date: LocalDate): Timestamp {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().let { Timestamp(it.epochSecond, it.nano) }
    }

    fun createTrip(title: String, destinationId: String, startDate: LocalDate, endDate: LocalDate, nextRoute: String) {
        Log.d("TripViewModel", "Launching coroutine to create trip with title: $title, start date: $startDate, end date: $endDate")
        viewModelScope.launch {
            _loading.value = true
            try {
                _trips.value += tripRepository.createTrip(
                    title = title,
                    startDate = convertLocalDate(startDate),
                    endDate = convertLocalDate(endDate),
                    destinationId = destinationId,
                )
            } catch (e: Exception) {
                Log.d("TripViewModel", "Failed to create trip: $e")
                _errors.send(e.message ?: "Failed to create trip")
                _loading.value = false
                return@launch
            }
            Log.d("TripViewModel", "Created trip with title: $title, start date: $startDate, end date: $endDate successfully")
            _loading.value = false
            _navDestination.send(nextRoute)
        }
    }

    fun getListOfTrips() {
        Log.d("TripViewModel", "Launching coroutine to get list of trips")
        viewModelScope.launch {
            _loading.value = true
            try {
                _trips.value = tripRepository.getListOfTrips()
            } catch (e: Exception) {
                Log.d("TripViewModel", "Failed to get list of trips: $e")
                _errors.send(e.message ?: "Failed to get list of trips")
                _loading.value = false
                return@launch
            }
            Log.d("TripViewModel", "Got list of trips successfully: ${_trips.value}")
            _loading.value = false
        }
    }

    fun getTripById(id: String) {
        Log.d("TripViewModel", "Launching coroutine to get trip with id: $id")
        viewModelScope.launch {
            _loading.value = true
            try {
                _trip.value = tripRepository.getTripById(id)
            } catch (e: Exception) {
                Log.d("TripViewModel", "Failed to get trip with id: $id: $e")
                _errors.send(e.message ?: "Failed to get trip with id: $id")
                _loading.value = false
                return@launch
            }
            Log.d("TripViewModel", "Got trip with id: $id successfully")
            _loading.value = false
        }
    }

    fun updateTrip(trip: Trip, nextRoute: String) {
        Log.d("TripViewModel", "Launching coroutine to update trip with id: ${trip.id}")
        Log.d("TripViewModel", "Trip: $trip")
        viewModelScope.launch {
            _loading.value = true
            try {
                tripRepository.updateTrip(trip)
                _trip.value = trip
            } catch (e: Exception) {
                Log.d("TripViewModel", "Failed to update trip with id: ${trip.id}: $e")
                _errors.send(e.message ?: "Failed to update trip with id: ${trip.id}")
                _loading.value = false
                return@launch
            }
            Log.d("TripViewModel", "Updated trip with id: ${trip.id} successfully")
            _loading.value = false
            _navDestination.send(nextRoute)
        }
    }

    fun addPlaceToTrip(tripId: String, placeId: String, dayId: String, nextRoute: String) {
        // need to create place from Google place api
        PlacesApi.getPlaceFromId(placeId) {
            Log.d("TripViewModel", "Got place from id: $placeId: $it")
            Log.d("TripViewModel", "Launching coroutine to add place with id: $placeId to trip with id: $tripId")
            viewModelScope.launch {
                _loading.value = true
                try {
                    _trip.value = tripRepository.addPlaceToTrip(tripId, it, dayId)
                    Log.d("AddPlaceModel", "${trip.value}")
                } catch (e: Exception) {
                    Log.d("TripViewModel", "Failed to add place with id: $placeId to trip with id: $tripId: $e")
                }
                Log.d("TripViewModel", "Added place with id: $placeId to trip with id: $tripId successfully")
                _loading.value = false
                _navDestination.send(nextRoute)
            }
        }
    }

    fun deleteTrip(tripId: String) {
        Log.d("TripViewModel", "Launching coroutine to delete trip with id: $tripId")
        val originalValue = _trips.value
        _trips.value = _trips.value.filter { it.id != tripId }
        viewModelScope.launch {
            try {
                tripRepository.deleteTrip(tripId)
                _trips.value = tripRepository.getListOfTrips()
            } catch (e: Exception) {
                Log.d("TripViewModel", "Failed to delete trip with id: $tripId: $e")
                _trips.value = originalValue
                return@launch
            }
            Log.d("TripViewModel", "Deleted trip with id: $tripId successfully")
        }
    }

    fun getDayToPlaceMapping(): Map<ItineraryDay, List<Place>> {
        val dayToPlaceMapping = linkedMapOf<ItineraryDay, List<Place>>()
        val placesById = trip.value?.places?.associateBy { it.id }
        val days =  trip.value?.days?.sortedBy { it.date }
        days?.forEach { day ->
            val dayPlaces = day.placeIds.mapNotNull { placeId -> placesById?.get(placeId) }
            dayToPlaceMapping[day] = dayPlaces
        }

        return dayToPlaceMapping
    }

    fun resetTrip() {
        _trip.value = null
    }

    fun userLogOut() {
        _trips.value = emptyList()
        _trip.value = null
        _loading.value = false
    }
}
