package com.tera.weather_open_meteo.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
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
import com.tera.weather_open_meteo.widgets.Widget
import com.tera.weather_open_meteo.widgets.WidgetTab
import com.tera.weather_open_meteo.models.CurrentModel
import com.tera.weather_open_meteo.models.DaysModel
import org.json.JSONObject
import java.util.Locale
import java.util.TimeZone

class Weather(val context: Context) {

    companion object {
        const val FORMAT_DATE = "yyyy-MM-dd HH:mm"
    }

    private val sp = context.getSharedPreferences(MyConst.SETTING, Context.MODE_PRIVATE)

    private var activity = MainActivity()
    private var latitude = 0.0 // Широта
    private var longitude = 0.0 // Долгота
    private var city: String? = null
    private var timeZone = ""
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
            activity.setProgressBar(context, 0, 0, true)
            return
        }

        // Проверить подключение к Интернет
        if (!MyLocation.isOnline(context)) {
//            val message = context.getString(R.string.no_net)
//            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            activity.setProgressBar(context, 0, 0, true)
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
                    //Log.d("myLogs", "getLocation 1, latitude: $latitude, longitude: $longitude")
                    getCity()
                } else
                    activity.setProgressBar(context, 0, 0, true)
            }
    }

    // Получить город
    private fun getCity() {
        //Log.d("myLogs", "getCity 1, lat: $latitude, long: $longitude, city: $city")
        Geocoder(context, Locale.getDefault())
            .getAddress(latitude, longitude) { address: android.location.Address? ->
                city = address?.locality
                if (city != null) {
                    //Log.d("myLogs", "getCity 2, lat: $latitude, long: $longitude, city: $city")
                    saveLocation()
                    updateWeather(0, 0)
                } else
                    activity.setProgressBar(context, 0, 0, true)
            }
    }

    @Suppress("DEPRECATION")
    private fun Geocoder.getAddress(
        latitude: Double, longitude: Double,
        address: (android.location.Address?) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getFromLocation(latitude, longitude, 1) { address(it.firstOrNull()) }
            return
        }

        try {
            address(getFromLocation(latitude, longitude, 1)?.firstOrNull())
        } catch (e: Exception) {
            address(null)
        }
    }

    private fun saveLocation() {
        sp.edit {
            putFloat(MyConst.LATITUDE, latitude.toFloat())
            putFloat(MyConst.LONGITUDE, longitude.toFloat())
            putString(MyConst.CITY, city)
            putString(MyConst.TIME_ZONE, timeZone)
        }
    }

    // Получить погоду из Widget
    fun getWeatherWidget(id: Int, num: Int, keyButton: Boolean) {

        timeLastUpdate = sp.getLong(MyConst.TIME_LAST, 0L)
        val period = sp.getInt(MyConst.PERIOD, 30)

        val timeNew = MyLocation.currentTimeMillis()
        val timeDiff = (timeNew - timeLastUpdate) / 60000f

        //Log.d("myLogs", "getWeatherWidget 1, timeDiff: $timeDiff")

        if (timeDiff < period && !keyButton) {
//            val message = context.getString(R.string.period_update) + " $period min"
//            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            activity.setProgressBar(context, id, num, false)
            return
        }

        city = sp.getString(MyConst.CITY, null)

        if (city == null) {
            activity.setProgressBar(context, id, num, false)
//            val message = "Error Widget, City = null"
//            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            return
        }
        latitude = sp.getFloat(MyConst.LATITUDE, 0f).toDouble()
        longitude = sp.getFloat(MyConst.LONGITUDE, 0f).toDouble()
        timeZone = sp.getString(MyConst.TIME_ZONE, "").toString()

        updateWeather(id, num)
    }

    private fun loadParams() {
        numTemp = sp.getInt(MyConst.NUM_TEMP, 0)
        numPress = sp.getInt(MyConst.NUM_PRESS, 0)
        numWind = sp.getInt(MyConst.NUM_WIND, 0)
    }

    private fun retryUpdate(context: Context, id: Int, num: Int) {
        Handler(Looper.getMainLooper()).postDelayed({
            updateWeather(id, num)
        }, 5000)
    }

    // Обновить погоду
    private fun updateWeather(id: Int, num: Int) {
//        val timeUpdate = MyLocation.getCurrentTime("H:mm:ss")
//        Log.d("myLogs", "updateWeathe, $timeUpdate, Запрос, num: $num")
        loadParams()

        val url = "https://api.open-meteo.com/v1/forecast?" +
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
                if (num == 0) {
                    hoursWeather(obj)
                    daysWeather(obj)
                }
            },
            { _ ->
//                val timeError = MyLocation.getCurrentTime("H:mm:ss")
//                Log.d("myLogs", "updateWeather, $timeError, Запрос: $countRequest, Ошибка")

                if (countRequest > 0) {
                    retryUpdate(context, id, num)
                    countRequest--
                } else { // Загрузить старые значения
                    if (num != 0) {
                        val gson = Gson()
                        val currentStr = sp.getString(MyConst.LIST_CURR, "")
                        val listCurrent = gson.fromJson(currentStr, CurrentModel::class.java)

                        if (listCurrent != null)
                            setWeatherWidget(id, num, listCurrent, true)
                    }
                    activity.setProgressBar(context, id, num, false)

                    //Log.d("myLogs", "$timeUpdate, num: $num, Error connection!!")
//                    val message = context.getString(R.string.error_connect)
//                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            })
        queue.add(request)
    }

    private fun currentWeather(obj: JSONObject, id: Int, num: Int) {
        //Log.d("myLogs", "currentWeather 1, id: $id, num: $num")

        val convert = ConvertWeather(context)

        val current = obj.getJSONObject("current")
        val dateTemp = current.getString("time").replace('T', ' ')
        val time = convert.formatDate(dateTemp, FORMAT_DATE, "H:mm")

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
        sunrise = convert.formatDate(sunrise, FORMAT_DATE, "H:mm")
        label = context.getString(R.string.sunrise)
        sunrise = "$label $sunrise"

        // Закат
        var sunset = days.getJSONArray("sunset")[0].toString().replace('T', ' ')
        sunset = convert.formatDate(sunset, FORMAT_DATE, "H:mm")
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

//        Log.d("myLogs", "currentWeather 1, temp: $temp")
        activity.setProgressBar(context, id, num, false)

        if (num == 0) {
            activity.seCurrentView(context, list)
        } else {
            setWeatherWidget(id, num, list, false)
        }

        activity.saveCurrent(context, list)

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
        val hour = convert.getCurrentTime("H").toInt() + 1

        for ((index, i) in (hour..hour + 23).withIndex()) {
            val dateTime = timeArray[i].toString().replace('T', ' ')
            val time = convert.formatDate(dateTime, FORMAT_DATE, "H:mm")

            val tempC = tempsArray[i].toString().toDouble()
            val temp = convert.getTemperature(tempC, numTemp)
            val icon = convert.getIcon(isDayArray[i].hashCode(), codeArray[i].hashCode())

            val timeInt = time.filter { it.isDigit() }.toInt()
            if (timeInt == 0) {
                indexZero = index
                dayWeek = convert.formatDate(dateTime, FORMAT_DATE, "EEE.")
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
            val date = convert.formatDate(dateTime, format, "d LLL")
            var nameDay = convert.formatDate(dateTime, format, "EEE.") // День недели
            nameDay = nameDay.replaceFirstChar { it.uppercase() }
            val numDay = convert.getNumDayWeek(dateTime) // Номер дня недели

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

    // Ширина, высота ImageView, макс., минн. температуры
    private fun listParam(tempMax: Int, tempMin: Int): ArrayList<Int> {
        val list = ArrayList<Int>()
        // Получить размеры ImageView
        val w = context.resources.getDimension(R.dimen.width_im_list) // В пикселах
        val h = context.resources.getDimension(R.dimen.height_im_list)

        val res = context.resources.displayMetrics
        val sp = res.density

        val wImage = (w / sp).toInt()  // Ширина ImageView
        val hImage = (h / sp).toInt()  // Высота ImageView

        list.add(wImage)
        list.add(hImage)
        list.add(tempMax)
        list.add(tempMin)
        //Log.d("myLogs", "list: ${list.toList()}")
        return list
    }

    // Установить значения текущей погоды в Widget
    private fun setWeatherWidget(id: Int, num: Int, list: CurrentModel, error: Boolean) {
        if (!error) {
            timeLastUpdate = MyLocation.currentTimeMillis()
            sp.edit {
                putLong(MyConst.TIME_LAST, timeLastUpdate)
            }
        }
        when (num) {
            1 -> {
                val widget = Widget()
                widget.setView(context, id, list)
            }

            2 -> {
                val widget = WidgetTab()
                widget.setView(context, id, list)
            }
        }
    }

}