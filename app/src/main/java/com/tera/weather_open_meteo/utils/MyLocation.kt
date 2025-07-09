package com.tera.weather_open_meteo.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.tera.weather_open_meteo.city.timezone

object MyLocation {

    // Проверка разрешения. Если нет, запросить
    fun isPermissions(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Запросить разрешение
    fun permissionListener(context: Context) {
        context as Activity
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        ActivityCompat.requestPermissions(context, permissions, 0)
    }

    // Проверка включения GPS
    fun isLocationEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    // Включить GPS
    fun checkLocation(context: Context) {
        DialogManager.locationSettingsDialog(context, object : DialogManager.Listener {
            override fun onClick() {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        })
    }

    // Проверить подключение к Интернет
    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return true
            }
        }
        return false
    }

    @Suppress("DEPRECATION")
    fun getCity(context: Context, lat: Double, long: Double): String? {
        val geocoder = Geocoder(context)
        val addresses = geocoder.getFromLocation(lat, long, 1) ?: listOf()
        val address = addresses.firstOrNull()
        val city = address?.locality
        return city
    }

    @Suppress("DEPRECATION")
    fun getAddress(context: Context, lat: Double, long: Double): Pair<String, String>  {
        val geocoder = Geocoder(context)
        val addresses = geocoder.getFromLocation(lat, long, 1) ?: listOf()
        val address = addresses.firstOrNull()

        var region = address?.getAddressLine(0)
        var city = address?.locality
        if (city == null)
            city = region?.substringBefore(',') ?: ""

        if (region == null)
            region = ""

        return Pair(city, region)
    }

    @Suppress("DEPRECATION")
    fun getRegion(context: Context, lat: Double, long: Double): String {
        val geocoder = Geocoder(context)
        val addresses = geocoder.getFromLocation(lat, long, 1) ?: listOf()
        val address = addresses.firstOrNull()
        val city = address?.locality
        val admin = address?.adminArea
        val country = address?.countryName
        return if (admin == null || country == null)
            city.toString()
        else
            "$admin, $country"
    }

    @Suppress("DEPRECATION")
    fun getTimeZone(context: Context, lat: Double, long: Double): String {
        val geocoder = Geocoder(context)
        val addresses = geocoder.getFromLocation(lat, long, 1) ?: listOf()
        val address = addresses.firstOrNull()
        var timeZone = address?.timezone(context)
        if (timeZone == null)
            timeZone = MyConst.TIME_ZONE_NO
        return timeZone
    }


}