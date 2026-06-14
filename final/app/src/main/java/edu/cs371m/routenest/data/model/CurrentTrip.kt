package edu.cs371m.routenest.data.model

data class CurrentTrip (
    val places: List<Place> = emptyList(),
    val days: List<ItineraryDay> = emptyList(),
)
