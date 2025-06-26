package com.tera.weather_open_meteo.city

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.tera.weather_open_meteo.models.CityModel
import com.tera.weather_open_meteo.utils.ConvertWeather
import com.tera.weather_open_meteo.utils.MyConst
import org.json.JSONObject

class WeatherCity() {

    fun getWeatherTemp(context: Context, list: ArrayList<CityModel>, index: Int) {

        val latitude = list[index].latitude
        val longitude = list[index].longitude
        val timeZone = list[index].timeZone

        val url = MyConst.URL_API +
                "latitude=$latitude&" +
                "longitude=$longitude&" +
                "timezone=$timeZone&" +
                "current=temperature_2m,is_day,weather_code"// +

        val queue = Volley.newRequestQueue(context)
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                val obj = JSONObject(response)
                currentWeather(context, obj, list, index)
            },
            {
                Log.d("myLogs", "Error: $it")
            })
        queue.add(stringRequest)
    }

    // Текущая погода городов
    private fun currentWeather(
        context: Context, obj: JSONObject, list: ArrayList<CityModel>,
        index: Int
    ) {
        val convert = ConvertWeather(context)
        val current = obj.getJSONObject("current")
        val temp = convert.getTemperature(current.getDouble("temperature_2m"), 0)

        // Состояние
        val isDay = current.getInt("is_day")
        val code = current.getInt("weather_code")
        val icon = convert.getIcon(isDay, code)

        list[index].temp = temp
        list[index].icon = icon

        val size = list.size
        if (index == size - 1)
            ListActivity().setWeather(context, list)

    }

}