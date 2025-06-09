package com.tera.weather_open_meteo.utils

import android.content.Context
import com.tera.weather_open_meteo.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class ConvertWeather(private val context: Context) {

    fun getTemperature(temp: Double, numTemp: Int ): String {
        val res = if (numTemp == 0)
            temp.roundToInt().toString() + "°C"
        else
            (temp * 1.8 + 32.0).roundToInt().toString() + "°F"
        return res
    }

    fun getMaxMinTemp(tMax: Float, tMin: Float, numTemp: Int ): String {
        var res = ""
        if (numTemp == 0){
            res = tMax.roundToInt().toString() + "/" + tMin.roundToInt().toString() + "°C"
        } else {
            val t1 = (tMax * 1.8 + 32.0).roundToInt().toString()
            val t2 = (tMin * 1.8 + 32.0).roundToInt().toString() + "°F"
            res = "$t1/$t2"
        }
        return res
    }

    // Давление
    fun getPress(value: Double, numPress: Int): String {
        var res = ""
        val label = context.getString(R.string.pressure)
        when(numPress) {
            0 -> {
                val press = value.roundToInt()
                val unit = context.getString(R.string.unit_press_hpa)
                res = "$label $press $unit"
            }
            1 -> {
                val press = value.roundToInt()
                val unit = context.getString(R.string.unit_press_mbr)
                res = "$label $press $unit"
            }
            2 -> {
                val press = (value * MyConst.HPA_MM_HG).roundToInt()
                val unit = context.getString(R.string.unit_press_mmHg)
                res = "$label $press $unit"
            }
        }
        return res
    }

    // Скорость ветра
    fun getWind(value: Double, dir: Int, numWind: Int): String{
        var res = ""
        val label = context.getString(R.string.wind)
        val windDir = getWindDir(dir)
        when(numWind) {
            0 -> {
                val speed = value.roundToInt()
                val unit = context.getString(R.string.unit_wind_kmh)
                res = "$label $speed $unit, $windDir"
            }
            1 -> {
                val speed = (value / 3.6).roundToInt()
                val unit = context.getString(R.string.unit_wind_mc)
                res = "$label $speed $unit, $windDir"
            }
            2 -> {
                val speed = (value * MyConst.KM_MIL).roundToInt()
                val unit = context.getString(R.string.unit_wind_mlh)
                res = "$label $speed $unit, $windDir"
            }
        }
        return res
    }

    // Направление ветра
    private fun getWindDir(dir: Int): String {
        var wordDir = ""
        wordDir = when (dir) {
            in 22..67 -> context.getString(R.string.ne)    // северо-восток
            in 68..112 -> context.getString(R.string.e)    // восток
            in 113..157 -> context.getString(R.string.se)  // юго-восток
            in 158..202 -> context.getString(R.string.s)   // юг
            in 203..247 -> context.getString(R.string.sw)  // юго-запад
            in 248..292 -> context.getString(R.string.w)   // запад
            in 293..337 -> context.getString(R.string.nw)  // северо-запад
            else -> context.getString(R.string.n)                // север
        }
        return wordDir
    }



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
    fun formatDate(date: String, format: String, field: String) : String{
        val dayOfWeek = getFromDateTime(date, format, field)
        return dayOfWeek.toString()
    }

    private fun getFromDateTime(dateTime: String, dateFormat: String, field: String): String? {
        val input = SimpleDateFormat(dateFormat, Locale.getDefault())
        val output = SimpleDateFormat(field, Locale.getDefault())
        try {
            val getAbbreviate = input.parse(dateTime)
            return getAbbreviate?.let { output.format(it) }
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return null
    }

    // Состояние погоды
    fun getCondition(code: Int): String {
        val condition = when (code) {
            0 -> context.getString(R.string.wmo0)    // 0. Ясно, clear
            1 -> context.getString(R.string.wmo1)    // 1. Малооблачно
            2 -> context.getString(R.string.wmo2)    // 2. Переменная облачность
            3 -> context.getString(R.string.wmo3)    // 3. Пасмурно
            45 -> context.getString(R.string.wmo45)  // 4. Туман
            48 -> context.getString(R.string.wmo48)  // 5. Туман и иней
            51 -> context.getString(R.string.wmo51)  // 6. Моросящий дождь
            53 -> context.getString(R.string.wmo53)  // 7. Умеренная морось
            55 -> context.getString(R.string.wmo55)  // 8. Интенсивная морось
            56 -> context.getString(R.string.wmo56)  // 9. Замерзающая морось
            57 -> context.getString(R.string.wmo57)  // 10. Замерзающая морось
            61 -> context.getString(R.string.wmo61)  // 11. Слабый дождь
            63 -> context.getString(R.string.wmo63)  // 12. Умеренный дождь
            65 -> context.getString(R.string.wmo65)  // 13. Сильный дождь
            66 -> context.getString(R.string.wmo66)  // 14. Ледяной дождь
            67 -> context.getString(R.string.wmo67)  // 15. Сильный ледяной дождь
            71 -> context.getString(R.string.wmo71)  // 16. Слабый снег
            73 -> context.getString(R.string.wmo73)  // 17. Умеренный снег
            75 -> context.getString(R.string.wmo75)  // 18. Сильный снегопад
            77 -> context.getString(R.string.wmo77)  // 19. Снежные зерна
            80 -> context.getString(R.string.wmo80)  // 20. Небольшие ливни
            81 -> context.getString(R.string.wmo81)  // 21. Умеренные ливни
            82 -> context.getString(R.string.wmo82)  // 22. Сильные ливни
            85 -> context.getString(R.string.wmo85)  // 23. Слабый снегопад
            86 -> context.getString(R.string.wmo86)  // 24. Сильный снегопад
            95 -> context.getString(R.string.wmo95)  // 25. Гроза
            96 -> context.getString(R.string.wmo96)  // 26. Гроза с градом
            99 -> context.getString(R.string.wmo99)  // 27. Гроза с градом
            else -> ""
        }
        return condition
    }

    fun getIcon(isDay: Int, code: Int): Int {
        return if (isDay == 1) getIconDay(code)
        else getIconNight(code)
    }

    // Иконки День
    private fun getIconDay(code: Int): Int {
        val rs = when (code) {
            0 -> R.drawable.day_0     // Ясно
            1, 2 -> R.drawable.day_1  // Малооблачно, Переменная облачность
            3 -> R.drawable.day_3     // Пасмурно
            45 -> R.drawable.day_45   // Туман
            48 -> R.drawable.day_48   // Туман
            51, 53, 55, 61, 63 -> R.drawable.day_51 // Моросящий дождь
            56, 57, 66 -> R.drawable.day_56 // Замерзающая морос
            65 -> R.drawable.day_65  // Сильный дождь
            67 -> R.drawable.day_67  // Сильный ледяной дождь
            71, 73, 85 -> R.drawable.day_71 // Слабый снег, Умеренный снег
            75, 86 -> R.drawable.day_75 // Сильный снегопад
            77 -> R.drawable.day_77 // Снежная крупа
            80 -> R.drawable.day_80 // Небольшие ливни
            81 -> R.drawable.day_81 // Умеренные ливни
            82 -> R.drawable.day_82 // Сильные ливни
            95 -> R.drawable.day_95 // Гроза
            96, 99 -> R.drawable.day_96 // Гроза с градом
            else -> R.drawable.unknown
        }
        return rs
    }

    // Иконки Ночь
    private fun getIconNight(code: Int): Int {
        val rs = when (code) {
            0 -> R.drawable.night_0    // Ясно
            1, 2 -> R.drawable.night_1 // Малооблачно, Переменная облачность
            3 -> R.drawable.night_3    // Пасмурно
            45 -> R.drawable.night_45  // Туман
            48 -> R.drawable.night_48  // Туман
            51, 53, 55, 61, 63 -> R.drawable.night_51 // Моросящий дождь
            56, 57, 66 -> R.drawable.night_56 // Замерзающая морос
            65 -> R.drawable.night_65 // Сильный дождь
            67 -> R.drawable.night_67 // Сильный ледяной дождь
            71, 73, 85 -> R.drawable.night_71 // Слабый снег, Умеренный снег
            75, 86 -> R.drawable.night_75 // Сильный снегопад
            77 -> R.drawable.night_77 // Снежная крупа
            80 -> R.drawable.night_80 // Небольшие ливни
            81 -> R.drawable.night_81 // Умеренные ливни
            82 -> R.drawable.night_82 // Сильные ливни
            95 -> R.drawable.night_95 // Гроза
            96, 99 -> R.drawable.night_96 // Гроза с градом
            else -> R.drawable.unknown
        }
        return rs
    }

}