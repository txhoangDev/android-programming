package edu.cs371m.routenest.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Trip(
    @DocumentId val id: String = "",
    val title: String = "",
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now(),
    val notes: String = "",
    val destinationId: String = "",
    val places: List<Place> = emptyList(),
    val days: List<ItineraryDay> = emptyList(),
)
