package com.tera.weather_open_meteo.models

data class CityModel(
    var name: String,
    var region: String,
    var date: String,
    val latitude: Double,
    val longitude: Double,
    val timeZone: String,
    var temp: String,
    var icon: Int
)
