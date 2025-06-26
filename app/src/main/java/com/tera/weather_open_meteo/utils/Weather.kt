package com.tera.weather_open_meteo.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.Gson
import com.tera.weather_open_meteo.MainActivity
import com.tera.weather_open_meteo.R
import com.tera.weather_open_meteo.models.CityModel
import com.tera.weather_open_meteo.models.CurrentModel
import com.tera.weather_open_meteo.models.DaysModel
import com.tera.weather_open_meteo.widgets.Widget
import com.tera.weather_open_meteo.widgets.WidgetTab
import org.json.JSONObject
import java.util.TimeZone
import kotlin.concurrent.thread

class Weather(val context: Context) {

    private var pattern = MyConst.PATTERN_FORMAT
    private val sp = context.getSharedPreferences(MyConst.SETTING, Context.MODE_PRIVATE)

    private var activity = MainActivity()
    private var latitude = 0.0 // Широта
    private var longitude = 0.0 // Долгота
    private var city: String? = null
    private var timeZone = ""
    private var region = ""
    private var numTemp = 0
    private var numPress = 0
    private var numWind = 0
    private var countRequest = 2
    private var timeLastUpdate = 0L

    // Получить погоду
    @SuppressLint("MissingPermission")
    fun getWeatherMain() {

        //Log.d("myLogs", "getWeather, id: $id, num: $num")

        // Проверка включения GPS
        if (!MyLocation.isLocationEnabled(context)) {
            MyLocation.checkLocation(context)
            activity.setProgressBar(context, 0, 0, false)
            return
        }

        // Проверить подключение к Интернет
        if (!MyLocation.isOnline(context)) {
            activity.setProgressBar(context, 0, 0, false)
            return
        }

        timeZone = TimeZone.getDefault().id
        // Поучить местоположение, город, погоду
        getLocation()
    }

    // Получить местоположение
    private fun getLocation() {
        if (ActivityCompat
                .checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(
                    context, android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val ct = CancellationTokenSource()
        fusedLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token)
            .addOnCompleteListener {
                if (it.result != null) {
                    latitude = it.result.latitude
                    longitude = it.result.longitude
                    getCity()
                } else
                    activity.setProgressBar(context, 0, 0, true)
            }
    }

    // Получить место
    private fun getCity() {
        thread {
            city = MyLocation.getCity(context, latitude, longitude)
            if (city != null) {
                region = MyLocation.getRegion(context, latitude, longitude)
                saveLocation()
                updateWeather(0, 0)
            }
        }
    }

    private fun saveLocation() {
        sp.edit {
            putFloat(MyConst.LATITUDE, latitude.toFloat())
            putFloat(MyConst.LONGITUDE, longitude.toFloat())
            putString(MyConst.CITY, city)
            putString(MyConst.TIME_ZONE, timeZone)
            putString(MyConst.REGION, region)
        }
    }

    // Получить погоду из Widget
    fun getWeatherWidget(id: Int, num: Int, keyButton: Boolean) {

        timeLastUpdate = sp.getLong(MyConst.TIME_LAST, 0L)
        val period = sp.getInt(MyConst.PERIOD, 30)

        val timeNew = ConvertDate.currentTimeMillis()
        val timeDiff = (timeNew - timeLastUpdate) / 60000f

        if (timeDiff < period && !keyButton) {
            activity.setProgressBar(context, id, num, false)
            return
        }

        city = sp.getString(MyConst.CITY, null)

        if (city == null) {
            activity.setProgressBar(context, id, num, false)
            return
        }
        latitude = sp.getFloat(MyConst.LATITUDE, 0f).toDouble()
        longitude = sp.getFloat(MyConst.LONGITUDE, 0f).toDouble()
        timeZone = sp.getString(MyConst.TIME_ZONE, "").toString()

        updateWeather(id, num)
    }

    // Получить погоду в другом городе
    fun getWeatherCity(list: ArrayList<CityModel>) {
        city = list[0].name
        latitude = list[0].latitude
        longitude = list[0].longitude
        timeZone = list[0].timeZone
        updateWeather(0, 1)
    }

    private fun loadParams() {
        numTemp = sp.getInt(MyConst.NUM_TEMP, 0)
        numPress = sp.getInt(MyConst.NUM_PRESS, 0)
        numWind = sp.getInt(MyConst.NUM_WIND, 0)
    }

    private fun retryUpdate(id: Int, num: Int) {
        Handler(Looper.getMainLooper()).postDelayed({
            updateWeather(id, num)
        }, 5000)
    }

    // Обновить погоду
    private fun updateWeather(id: Int, num: Int) {
        loadParams()

        val url = MyConst.URL_API +
                "latitude=$latitude&" +
                "longitude=$longitude&" +
                "timezone=$timeZone&" +
                "hourly=temperature_2m,weather_code,is_day&" +
                "current=temperature_2m,relative_humidity_2m,is_day,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m&" +
                "daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,wind_speed_10m_max,wind_direction_10m_dominant,surface_pressure_mean"

        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                //Log.d("myLogs", "updateWeather, response: $response")
                countRequest = 0
                val obj = JSONObject(response)
                currentWeather(obj, id, num)
                if (num == 0 || num == 1) {
                    hoursWeather(obj)
                    daysWeather(obj)
                }
            },
            { _ ->

                if (countRequest > 0) {
                    retryUpdate(id, num)
                    countRequest--
                } else { // Загрузить старые значения в видженты
                    if (num == 2 || num == 3) {
                        val gson = Gson()
                        val currentStr = sp.getString(MyConst.LIST_CURR, "")
                        val listCurrent = gson.fromJson(currentStr, CurrentModel::class.java)

                        if (listCurrent != null)
                            setWeatherWidget(id, num, listCurrent, true)
                    }
                    activity.setProgressBar(context, id, num, false)
                }
            })
        queue.add(request)
    }

    private fun currentWeather(obj: JSONObject, id: Int, num: Int) {
//        Log.d("myLogs", "currentWeather 1, id: $id, num: $num")

        val convert = ConvertWeather(context)

        val current = obj.getJSONObject("current")
        val dateTemp = current.getString("time").replace('T', ' ')
        val time = ConvertDate.formatDate(dateTemp, pattern, "H:mm")

        val temp = convert.getTemperature(current.getDouble("temperature_2m"), numTemp)
        var humidity = current.getString("relative_humidity_2m") + "%"// Влажность
        var label = context.getString(R.string.humidity)
        humidity = "$label $humidity"

        val isDay = current.getInt("is_day")
        val code = current.getInt("weather_code")
        val pressureHPa = current.getDouble("surface_pressure")
        val pressure = convert.getPress(pressureHPa, numPress)
        val windSpeedKmH = current.getDouble("wind_speed_10m")
        val windDir = current.getInt("wind_direction_10m")
        val windSpeed = convert.getWind(windSpeedKmH, windDir, numWind)

        // Состояние
        val condition = convert.getCondition(code)
        val icon = convert.getIcon(isDay, code)

        // Дни
        val days = obj.getJSONObject("daily")
        val tempsMaxC = days.getJSONArray("temperature_2m_max")
        val tempsMinC = days.getJSONArray("temperature_2m_min")

        val tMaxC = tempsMaxC[0].toString().toFloat()
        val tMinC = tempsMinC[0].toString().toFloat()
        val maxMin = convert.getMaxMinTemp(tMaxC, tMinC, numTemp)

        // Восход
        var sunrise = days.getJSONArray("sunrise")[0].toString().replace('T', ' ')
        sunrise = ConvertDate.formatDate(sunrise, pattern, "H:mm")
        label = context.getString(R.string.sunrise)
        sunrise = "$label $sunrise"

        // Закат
        var sunset = days.getJSONArray("sunset")[0].toString().replace('T', ' ')
        sunset = ConvertDate.formatDate(sunset, pattern, "H:mm")
        label = context.getString(R.string.sunset)
        sunset = "$label $sunset"

        val list = CurrentModel(
            city!!,
            time,
            condition,
            temp,
            maxMin,
            icon,
            windSpeed,
            pressure, // Давление
            humidity, // Влажность
            sunrise,
            sunset
        )

//        Log.d("myLogs", "currentWeather, temp: $temp")
        activity.setProgressBar(context, id, num, false)

        when (num) {
            0 -> {
                activity.setCurrentWeather(context, list)
                saveHomeTempIcon(temp, icon)
            }

            1 -> activity.setCurrentWeather(context, list)
            2, 3 -> setWeatherWidget(id, num, list, false)
        }

        if (num != 1)
            activity.saveCurrent(context, list)

    }

    private fun saveHomeTempIcon(temp: String, icon: Int) {
        sp.edit {
            putString(MyConst.TEMP, temp)
            putInt(MyConst.ICON, icon)
        }
    }

    private fun hoursWeather(obj: JSONObject) {

        val listTemp = ArrayList<String>()
        val listTime = ArrayList<String>()
        val listIcon = ArrayList<Int>()
        var indexZero = 0
        var dayWeek = ""

        val convert = ConvertWeather(context)
        val hours = obj.getJSONObject("hourly")
        val timeArray = hours.getJSONArray("time")
        val tempsArray = hours.getJSONArray("temperature_2m")
        val codeArray = hours.getJSONArray("weather_code")
        val isDayArray = hours.getJSONArray("is_day")

        // Получить текущий час
        val hour = ConvertDate.getCurrentTime("H").toInt() + 1

        for ((index, i) in (hour..hour + 23).withIndex()) {
            val dateTime = timeArray[i].toString().replace('T', ' ')
            val time = ConvertDate.formatDate(dateTime, pattern, "H:mm")

            val tempC = tempsArray[i].toString().toDouble()
            val temp = convert.getTemperature(tempC, numTemp)
            val icon = convert.getIcon(isDayArray[i].hashCode(), codeArray[i].hashCode())

            val timeInt = time.filter { it.isDigit() }.toInt()
            if (timeInt == 0) {
                indexZero = index
                dayWeek = ConvertDate.formatDate(dateTime, pattern, "EEE.")
                dayWeek = dayWeek.replaceFirstChar { it.uppercase() }
            }

            listTime.add(time)
            listTemp.add(temp)
            listIcon.add(icon)
        }

        val time = listTime[indexZero]
        listTime[indexZero] = "$dayWeek $time"

        activity.setChart3(context, listTime, listTemp, listIcon, false)
    }

    private fun daysWeather(obj: JSONObject) {
        val convert = ConvertWeather(context)
        val list = ArrayList<DaysModel>()
        val format = "yyyy-MM-dd"

        val days = obj.getJSONObject("daily")
        val datesArray = days.getJSONArray("time")

        val codes = days.getJSONArray("weather_code")
        val tempsMaxC = days.getJSONArray("temperature_2m_max")
        val tempsMinC = days.getJSONArray("temperature_2m_min")

        val windSpeedKmH = days.getJSONArray("wind_speed_10m_max")
        val windDir = days.getJSONArray("wind_direction_10m_dominant")

        val pressurePa = days.getJSONArray("surface_pressure_mean")

        for (i in 0..<codes.length()) {
            val dateTime = datesArray[i].toString()
            val date = ConvertDate.formatDate(dateTime, format, "d LLL")
            var nameDay = ConvertDate.formatDate(dateTime, format, "EEE.") // День недели
            nameDay = nameDay.replaceFirstChar { it.uppercase() }
            val numDay = ConvertDate.getNumDayWeek(dateTime) // Номер дня недели

            val tMaxC = tempsMaxC[i].toString().toFloat()
            val tMinC = tempsMinC[i].toString().toFloat()
            val maxMin = convert.getMaxMinTemp(tMaxC, tMinC, numTemp)
            val windSpeed =
                convert.getWind(
                    windSpeedKmH[i].toString().toDouble(),
                    windDir[i].toString().toInt(),
                    numWind
                )

            // Состояние
            val condition = convert.getCondition(codes[i].hashCode())
            val icon = convert.getIcon(1, codes[i].hashCode())

            val pressureDb = pressurePa[i].toString().toDouble()
            val pressure = convert.getPress(pressureDb, numPress)

            val item = DaysModel(
                date,
                condition,
                maxMin,
                icon,
                windSpeed,
                pressure,
                nameDay,
                numDay
            )
            list.add(item)
        }

        activity.setAdapterDays(context, list)

        //Log.d("myLogs", "day, temp: ${list[0]} ")
    }

    // Установить значения текущей погоды в Widget
    private fun setWeatherWidget(id: Int, num: Int, list: CurrentModel, error: Boolean) {
        if (!error) {
            timeLastUpdate = ConvertDate.currentTimeMillis()
            sp.edit {
                putLong(MyConst.TIME_LAST, timeLastUpdate)
            }
        }
        when (num) {
            2 -> {
                val widget = Widget()
                widget.setView(context, id, list)
            }

            3 -> {
                val widget = WidgetTab()
                widget.setView(context, id, list)
            }
        }
    }

}