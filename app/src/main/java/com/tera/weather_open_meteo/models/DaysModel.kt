package com.tera.weather_open_meteo.models

data class DaysModel(
    var date: String,       // Дата
    val condition: String,  // Состояние
    var maxMin: String,     // Макс/Мин температура
    val icon: Int,          // Картинка
    var windSpeed: String,  // Скорость ветра
    var pressure: String,   // Давление
    val nameDay: String,    // Название дня недели
    val numDay: Int         // Номер дня недели
)
