package com.tera.weather_open_meteo.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Converter {


    // Текущее время
    fun getCurrentTime(pattern: String): String {
        val stf = SimpleDateFormat(pattern, Locale.getDefault())
        return stf.format(Date()).toString()
    }
}