package edu.cs371m.routenest.data.model

data class Place(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val googlePlaceId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
)
