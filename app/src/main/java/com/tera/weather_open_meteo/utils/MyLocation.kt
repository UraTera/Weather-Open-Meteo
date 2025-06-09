package com.tera.weather_open_meteo.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

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

    // Включить GPS
    fun checkLocation(context: Context) {
        DialogManager.locationSettingsDialog(context, object : DialogManager.Listener {
            override fun onClick(name: String?) {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        })
    }

    // Получить диагональ экрана
    fun getScreenDiagonal(context: Context): Float {
        val screen = context.resources.displayMetrics
        val w = screen.widthPixels  // Width in pixels
        val h = screen.heightPixels
        val xdpi = screen.xdpi      // Density pixels per inch
        val ydpi = screen.ydpi
        val width = w / xdpi
        val height = h / ydpi
        val diagonalIn = sqrt((width.pow(2)) + (height.pow(2)))
        return diagonalIn
    }

    // Текущее время в формате
    fun getCurrentTime(pattern: String): String {
        val stf = SimpleDateFormat(pattern, Locale.getDefault())
        return stf.format(Date()).toString()
    }

    // Текущее время в миллисекундах
    fun currentTimeMillis(): Long {
        val calendar = Calendar.getInstance()
        val timeMillis = calendar.timeInMillis
        return timeMillis
    }

}