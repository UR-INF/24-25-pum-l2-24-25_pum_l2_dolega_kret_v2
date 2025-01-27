package com.example.appcar

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Trip(
    val startLocation: String = "",
    val destination: String = "",
    val distance: Double = 0.0,
    val fuelUsed: Double = 0.0,
    val date: String = "",
    val carName: String = "",
    val userId: String = "",
    val vehicleId: String = "", // Dodano pole vehicleId
    val id: String = ""
)
