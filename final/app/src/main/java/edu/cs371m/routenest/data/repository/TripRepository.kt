package edu.cs371m.routenest.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.cs371m.routenest.data.model.ItineraryDay
import edu.cs371m.routenest.data.model.Place
import edu.cs371m.routenest.data.model.Trip
import edu.cs371m.routenest.data.model.TripSummary
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

class TripRepository
@Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    private fun tripsRef(): CollectionReference {
        val currentUser = auth.currentUser?.uid ?: throw IllegalStateException("User must be logged in")
        Log.d("TripRepository", "Getting list of trips for user id: $currentUser")
        return db.collection("users").document(currentUser).collection("trips")
    }

    private fun mapTripToTripSummary(trip: Trip): TripSummary {
        val tripSum = TripSummary(
            id = trip.id,
            title = trip.title,
            dateRange = "${formatter.format(trip.startDate.toDate())} - ${formatter.format(trip.endDate.toDate())}",
            destinationId = trip.destinationId
        )
        return tripSum
    }

    private fun calculateNumOfDays(startDate: Timestamp, endDate: Timestamp): Long {
        val start = startDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val end = endDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        return ChronoUnit.DAYS.between(start, end)
    }

    // Create trip so need to return created object
    suspend fun createTrip(title: String, destinationId: String, startDate: Timestamp, endDate: Timestamp): TripSummary {
        Log.d("TripRepository", "Creating trip with title: $title, start date: $startDate, end date: $endDate")
        val tripRef = tripsRef().document()
        val localDate = startDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val daysCollection = tripRef.collection("days")

        // create batch to ensure atomic
        val batch = db.batch()

        val listOfDays = mutableListOf<ItineraryDay>()
        val numOfDays = calculateNumOfDays(startDate, endDate)
        Log.d("TripRepository", "Creating $numOfDays days for trip")
        for (i in 0..numOfDays) {
            val daysRef = daysCollection.document()
            val day = localDate.plusDays(i)

            Log.d("TripRepository", "Creating day with date: ${daysRef.id}")

            val itineraryDay = ItineraryDay(
                id = daysRef.id,
                date = Timestamp(Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant())),
                placeIds = emptyList(),
            )

            listOfDays.add(itineraryDay)
            batch.set(daysRef, itineraryDay)
        }

        val trip = Trip(
            id = tripRef.id,
            title = title,
            startDate = startDate,
            endDate = endDate,
            notes = "",
            destinationId = destinationId,
            days = listOfDays
        )

        batch.set(tripRef, trip)

        batch.commit().await()
        return mapTripToTripSummary(trip)
    }

    suspend fun getListOfTrips(): List<TripSummary> {
        return tripsRef().orderBy("startDate").get().await().toObjects(Trip::class.java).map {
            mapTripToTripSummary(it)
        }
    }

    suspend fun getTripById(id: String): Trip {
        Log.d("TripRepository", "Getting trip with id: $id")
        val tripDoc = tripsRef().document(id).get().await()
        val trip = tripDoc.toObject(Trip::class.java) ?: throw Exception("Trip not found")

        Log.d("TripRepository", "Got Trip with id: $trip")
        val daysCollection = tripsRef().document(id).collection("days").orderBy("date", Query.Direction.ASCENDING).get().await()
        val days = daysCollection.toObjects(ItineraryDay::class.java)

        return trip.copy(days = days)
    }

    suspend fun updateTrip(trip: Trip) {
        Log.d("TripRepository", "Updating trip with id: ${trip.id}")
        val tripRef = tripsRef().document(trip.id)

        tripRef.update(
            "notes", trip.notes,
            "places", trip.places,
            "days", trip.days
        ).await()

        trip.days.forEach { day ->
            Log.d("AddPlace", "Updating day with id: $day")
            tripRef.collection("days")
                .document(day.id)
                .set(day)
                .await()
        }
    }

    suspend fun addPlaceToTrip(tripId: String, place: com.google.android.libraries.places.api.model.Place, dayId: String): Trip {
        Log.d("TripRepository", "Adding place to trip with id: $tripId")
        val tripRef = tripsRef().document(tripId)
        val trip = tripRef.get().await().toObject(Trip::class.java) ?: throw Exception("Trip not found")
        val placesRef = tripRef.collection("places").document()
        val daysRef = tripRef.collection("days").document(dayId)

        val place = Place(
            id = placesRef.id,
            name = place.displayName ?: "",
            address = place.formattedAddress ?: "",
            googlePlaceId = place.id ?: "",
            latitude = place.location?.latitude ?: 0.0,
            longitude = place.location?.longitude ?: 0.0,
        )

        Log.d("TripRepository", "Creating place with id: ${placesRef.id}")

        val batch = db.batch()

        val updatedPlacesList = trip.places.toMutableList() + place
        Log.d("TripRepository", "Trying to find day with id: $dayId")
        val updatedItinerary = trip.days.map {
            if (it.id == dayId) {
                Log.d("TripRepository", "Found day with id: $dayId")
                it.copy(placeIds = it.placeIds + placesRef.id)
            } else {
                it
            }
        }

        val updatedTrip = trip.copy(places = updatedPlacesList, days = updatedItinerary)

        Log.d("TripRepository", "Updating trip with id: ${tripRef.id} with data: $updatedTrip")
        batch.set(tripRef, updatedTrip)
        Log.d("TripRepository", "Updating place with id: ${placesRef.id} with data: $place")
        batch.set(placesRef, place)

        val targetDayChange = updatedItinerary.find { it.id == dayId }
        if (targetDayChange != null) {
            Log.d("TripRepository", "Updating day with id: $dayId with data: $targetDayChange")
            batch.set(daysRef, targetDayChange)
        }

        batch.commit().await()
        return updatedTrip
    }

    suspend fun deleteTrip(tripId: String) {
        val batch = db.batch()
        val tripRef = tripsRef().document(tripId)

        Log.d("TripRepository", "Deleting trip with id: $tripId")
        val trip = tripRef.get().await().toObject(Trip::class.java) ?: throw Exception("Trip not found")
        trip.days.forEach { day ->
            Log.d("TripRepository", "Deleting day with id: ${day.id}")
            batch.delete(tripRef.collection("days").document(day.id))
        }

        trip.places.forEach { place ->
            Log.d("TripRepository", "Deleting place with id: ${place.id}")
            batch.delete(tripRef.collection("places").document(place.id))
        }

        batch.delete(tripRef)
        batch.commit().await()
    }
}
