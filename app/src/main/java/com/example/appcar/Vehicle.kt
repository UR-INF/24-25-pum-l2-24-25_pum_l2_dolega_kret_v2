package com.example.appcar

data class Vehicle(
    val id: String = "",
    val brand: String = "",
    val model: String = "",
    val year: Int = 0,
    val engineCapacity: Double = 0.0,
    val fuelType: String = "",
    val fuelConsumption: Double = 0.0,
    val userId: String = "",
    val lastService: String? = null
)