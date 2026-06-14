package edu.cs371m.routenest.data.api

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchResolvedPhotoUriRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

object PlacesApi {
    private var placesClient: PlacesClient? = null
    private var geoCoder: Geocoder? = null

    fun initialize(context: Context, apiKey: String) {
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(context, apiKey)
        }
        placesClient = Places.createClient(context)
        geoCoder = Geocoder(context)
    }

    // Getting the lat long from geocoder
    private fun getBoundedLocation(location: String, onSuccess: (List<Address>) -> Unit) {
        Log.d("PlacesApi", "Retrieving lat long from location: $location")
        geoCoder?.getFromLocationName(location, 1) { addresses ->
            onSuccess(addresses)
        }
    }

    fun getCityCoordinates(city: String, onSuccess: (LatLng) -> Unit) {
        Log.d("PlacesApi", "Retrieving lat long from city: $city")
        geoCoder?.getFromLocationName(city, 1) { addresses ->
            if (addresses.isNotEmpty())
                onSuccess(LatLng(addresses[0].latitude, addresses[0].longitude))
        }
    }

    // getting auto complete prediction from place API
    fun getAutoCompletePredictions(query: String, token: AutocompleteSessionToken, onSuccess: (List<AutocompletePrediction>) -> Unit) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setSessionToken(token)
            .setTypesFilter(listOf(PlaceTypes.CITIES))
            .build()

        Log.d("PlacesApi", "Retrieving AutoComplete predictions")
        placesClient?.findAutocompletePredictions(request)?.addOnSuccessListener { result ->
            Log.d("PlacesApi", "Successfully retrieved AutoComplete predictions: $result")
            onSuccess(result.autocompletePredictions)
        }?.addOnFailureListener {
            Log.d("PlacesApi", "Failed to retrieve AutoComplete predictions: $it")
        }
    }

    fun getAutoCompletePredictionsPlaces(query: String, token: AutocompleteSessionToken, location: String, onSuccess: (List<AutocompletePrediction>) -> Unit) {
        Log.d("PlacesApi", "Retrieving AutoComplete predictions for places bounded by location: $location")
        getBoundedLocation(location) {
            Log.d("PlacesApi", "Successfully retrieved lat long: $it")
            val address = it[0]
            val bounds = RectangularBounds.newInstance(
                LatLng(address.latitude - 0.05, address.longitude  - 0.05),
                LatLng(address.latitude  + 0.05, address.longitude  + 0.05)
            )
            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setSessionToken(token)
                .setLocationBias(bounds)
                .build()

            placesClient?.findAutocompletePredictions(request)?.addOnSuccessListener { result ->
                onSuccess(result.autocompletePredictions)
            }
        }
    }

    fun getImageFromId(id: String, onSuccess: (Uri?) -> Unit) {
        Log.d("PlacesApi", "Retrieving image from id: $id")
        val request = FetchPlaceRequest.newInstance(id, listOf(Place.Field.PHOTO_METADATAS))

        placesClient?.fetchPlace(request)?.addOnSuccessListener { result ->
            val photoMetadata = result.place.photoMetadatas?.firstOrNull() ?: return@addOnSuccessListener
            val photoRequest = FetchResolvedPhotoUriRequest.builder(photoMetadata)
                .build()

            placesClient?.fetchResolvedPhotoUri(photoRequest)?.addOnSuccessListener { response ->
                onSuccess(response.uri)
            }?.addOnFailureListener {
                Log.d("PlacesApi", "Failed to retrieve image: $it")
            }
        }?.addOnFailureListener {
            Log.d("PlacesApi", "Failed to retrieve image: $it")
        }
    }

    fun getPlaceFromId(id: String, onSuccess: (Place) -> Unit) {
        Log.d("PlacesApi", "Retrieving place from id: $id")
        val request = FetchPlaceRequest.newInstance(id, listOf(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LOCATION))
        placesClient?.fetchPlace(request)?.addOnSuccessListener { result ->
            Log.d("PlacesApi", "Successfully retrieved place: $result")
            onSuccess(result.place)
        }?.addOnFailureListener {
            Log.d("PlacesApi", "Failed to retrieve place: $it")
        }
    }
}