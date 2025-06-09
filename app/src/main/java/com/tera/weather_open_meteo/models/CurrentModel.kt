package com.tera.weather_open_meteo.models

data class CurrentModel(
    val city: String,        // Город
    var time: String,        // Время последнего обновления
    val condition: String,   // Состояние
    val currentTemp: String, // Текущая температура
    var maxMin: String,      // Макс/Мин температура
    val icon: Int,           // Картинка
    var windSpeed: String,   // Скорость ветра
    var pressure: String,    // Давление
    val humidity: String,    // Влажность
    var sunrise: String,     // Восход
    var sunset: String       // Закат
)
