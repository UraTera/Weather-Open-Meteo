package com.tera.weather_open_meteo.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tera.weather_open_meteo.widgets.Widget
import com.tera.weather_open_meteo.widgets.WidgetTab

class ScreenReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val sp = context.getSharedPreferences(MyConst.SETTING, Context.MODE_PRIVATE)
        val id = sp.getInt(MyConst.WIDGET_ID, 0)
        val num = sp.getInt(MyConst.WIDGET_NUM, 0)

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            updateWeather(context, id, num, true)
        }

        if (intent.action == Intent.ACTION_USER_PRESENT) {
            updateWeather(context, id, num, false)
        }
    }

    private fun updateWeather(context: Context, id: Int, num: Int, key: Boolean){
        when (num) {
            1 -> {
                val widget = Widget()
                widget.getWeather(context, id, key)
            }

            2 -> {
                val widget = WidgetTab()
                widget.getWeather(context, id, key)
            }
        }
    }


}