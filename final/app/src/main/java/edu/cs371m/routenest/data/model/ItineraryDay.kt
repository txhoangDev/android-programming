package edu.cs371m.routenest.data.model

import com.google.firebase.Timestamp

/**
 * Stored under: users/{uid}/trips/{tripId}/itineraryDays/{dayId}
 *
 * Recommended: use an ISO-8601 LocalDate string (e.g. "2026-04-12") as [id] so upserts are easy.
 */
data class ItineraryDay(
    val id: String = "",
    val date: Timestamp = Timestamp.now(),
    val placeIds: List<String> = emptyList(),
)
