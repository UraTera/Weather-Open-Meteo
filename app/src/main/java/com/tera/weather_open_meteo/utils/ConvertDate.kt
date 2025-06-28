package com.tera.weather_open_meteo.utils

import android.content.Context
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.pow
import kotlin.math.sqrt

object ConvertDate {

    private var pattern = MyConst.PATTERN_FORMAT
    private var dateFormat = MyConst.DATE_FORMAT

    // Текущее время
    fun getCurrentTime(pattern: String): String {
        val stf = SimpleDateFormat(pattern, Locale.getDefault())
        return stf.format(Date()).toString()
    }

    // Номер дня недели
    fun getNumDayWeek(time: String): Int {
        val calendar = Calendar.getInstance()
        val pattern = "yyyy-MM-dd"
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        val date = dateFormat.parse(time)
        val mls = date?.time
        calendar.timeInMillis = mls!!
        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    // Формат Даты и времени
    fun formatDate(date: String, pattern: String, field: String) : String{
        val dayOfWeek = getFromDateTime(date, pattern, field)
        return dayOfWeek.toString()
    }

    // Дата TimeZone
    fun dateTimeZone(zone: String) : String {
        val timeZone = TimeZone.getTimeZone(zone)
        val calendar = Calendar.getInstance(timeZone)
        val year = calendar.get(Calendar.YEAR )
        val month = calendar.get(Calendar.MONTH ) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH )
        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        if (zone == MyConst.TIME_ZONE_NO)
            hour += 1
        val minute = calendar.get(Calendar.MINUTE)
        val dateTime = "$year-$month-$day $hour:$minute"
        val date = getFromDateTime(dateTime, pattern, dateFormat)
        return date.toString()
    }

    private fun getFromDateTime(dateTime: String, pattern: String, dateFormat: String): String? {
        val input = SimpleDateFormat(pattern, Locale.getDefault())
        val output = SimpleDateFormat(dateFormat, Locale.getDefault())
        try {
            val getAbbreviate = input.parse(dateTime)
            return getAbbreviate?.let { output.format(it) }
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return null
    }

    // Текущее время в миллисекундах
    fun currentTimeMillis(): Long {
        val calendar = Calendar.getInstance()
        val timeMillis = calendar.timeInMillis
        return timeMillis
    }

    // Получить диагональ экрана
    fun getScreenDiagonal(context: Context): Float {
        val screen = context.resources.displayMetrics
        val w = screen.widthPixels
        val h = screen.heightPixels
        val xdpi = screen.xdpi
        val ydpi = screen.ydpi
        val width = w / xdpi
        val height = h / ydpi
        val diagonalIn = sqrt((width.pow(2)) + (height.pow(2)))
        return diagonalIn
    }

}