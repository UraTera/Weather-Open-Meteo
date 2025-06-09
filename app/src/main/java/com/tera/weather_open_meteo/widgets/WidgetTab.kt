package com.tera.weather_open_meteo.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.provider.AlarmClock
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.edit
import com.tera.weather_open_meteo.MainActivity
import com.tera.weather_open_meteo.R
import com.tera.weather_open_meteo.models.CurrentModel
import com.tera.weather_open_meteo.utils.MyConst
import com.tera.weather_open_meteo.utils.Weather
import com.tera.weather_open_meteo.utils.ScreenReceiver

private const val UPDATE_WEATHER_TAB_2 = "update_weather_tab_2"
private const val OPEN_CLOCK_TAB_2 = "open_clock_tab_2"
private val receiver2 = ScreenReceiver()

class WidgetTab : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetID: Int
    ) {

        val sp = context.getSharedPreferences(MyConst.SETTING, Context.MODE_PRIVATE)
        sp.edit {
            putInt(MyConst.WIDGET_ID, widgetID)
            putInt(MyConst.WIDGET_NUM, 2)
        }

        // Снять регистрацию
        unRegReceiver(context)
        // Регистрировать
        regReceiver(context)

        val views = RemoteViews(context.packageName, R.layout.widget_tab)

        // Открыть MainActivity
        val flag = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val intent = Intent(context, MainActivity::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
        intent.putExtra(MyConst.WIDGET_ID, widgetID)
        intent.putExtra(MyConst.WIDGET_NUM, 2)

        var pendingIntent = PendingIntent.getActivity(context, widgetID, intent, flag)
        views.setOnClickPendingIntent(R.id.root, pendingIntent)
        views.setOnClickPendingIntent(R.id.image, pendingIntent)

        // Обновить погоду
        val updateIntent = Intent(context, WidgetTab::class.java)
        updateIntent.action = UPDATE_WEATHER_TAB_2
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
        pendingIntent = PendingIntent.getBroadcast(context, widgetID, updateIntent, flag)
        views.setOnClickPendingIntent(R.id.flUpdate, pendingIntent)

        // Открыть часы
        updateIntent.action = OPEN_CLOCK_TAB_2
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID)
        pendingIntent = PendingIntent.getBroadcast(context, widgetID, updateIntent, flag)
        views.setOnClickPendingIntent(R.id.tcTime, pendingIntent)

        // Получить погоду
        getWeather(context, widgetID, true)

        appWidgetManager.updateAppWidget(widgetID, views)
    }

    // Получить погоду
    fun getWeather(context: Context, widgetID: Int, keyButton: Boolean) {
        Weather(context).getWeatherWidget(widgetID, 2, keyButton)
    }

    // Установить значения в View
    fun setView(context: Context, widgetID: Int, list: CurrentModel) {

        // Отключить ProgressBar
        setProgress(context, widgetID, false)
        val views = RemoteViews(context.packageName, R.layout.widget)
        val appWidgetManager = AppWidgetManager.getInstance(context)

        views.setTextViewText(R.id.tvCity, list.city)
        views.setTextViewText(R.id.tvDateUpdate, list.time) // Дата обновления
        views.setTextViewText(R.id.tvTemp, list.currentTemp)
        views.setTextViewText(R.id.tvMinMax, list.maxMin)  // Max / Min
        views.setTextViewText(R.id.tvHumidity, list.humidity) // Влажность
        views.setTextViewText(R.id.tvPressure, list.pressure) // Давление
        views.setTextViewText(R.id.tvWind, list.windSpeed) // Скорость ветра
        views.setTextViewText(R.id.tvCondition, list.condition) // Состояние погоды
        views.setImageViewResource(R.id.image, list.icon) // Иконка

        // Отключить ProgressBar
        setProgress(context, widgetID, false)

        appWidgetManager.partiallyUpdateAppWidget(widgetID, views)
    }

    // Установить прогресс бар
    fun setProgress(context: Context, widgetID: Int, keyOn: Boolean) {
        val views = RemoteViews(context.packageName, R.layout.widget)
        val appWidgetManager = AppWidgetManager.getInstance(context)

        if (keyOn) {
            Handler(Looper.getMainLooper()).postDelayed({
                // Отключить ProgressBar
                views.setViewVisibility((R.id.pbProgress), View.INVISIBLE)
                views.setViewVisibility((R.id.imProgress), View.VISIBLE)
                // Обновить
                appWidgetManager.partiallyUpdateAppWidget(widgetID, views)
            }, 15000)
        }

        if (keyOn) { // Включить
            views.setViewVisibility((R.id.pbProgress), View.VISIBLE)
            views.setViewVisibility((R.id.imProgress), View.INVISIBLE)
        } else {
            views.setViewVisibility((R.id.pbProgress), View.INVISIBLE)
            views.setViewVisibility((R.id.imProgress), View.VISIBLE)
        }
        // Обновить
        appWidgetManager.partiallyUpdateAppWidget(widgetID, views)
    }

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent != null) {
            // Обновить погоду
            if (intent.action.equals(UPDATE_WEATHER_TAB_2)) {
                // извлекаем ID экземпляра
                var widgetID = AppWidgetManager.INVALID_APPWIDGET_ID
                val extras = intent.extras
                if (extras != null) {
                    widgetID = extras.getInt(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID
                    )
                }
                setProgress(context, widgetID, true)
                getWeather(context, widgetID, true)
            }
            // Открыть часы
            if (intent.action.equals(OPEN_CLOCK_TAB_2)) {
                val openClockIntent = Intent(AlarmClock.ACTION_SET_TIMER)
                openClockIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(openClockIntent)
            }
        }
    }

    // Регистрация BroadcastReceiver
    private fun regReceiver(context: Context){
        context.applicationContext.registerReceiver(
            receiver2,
            IntentFilter(Intent.ACTION_USER_PRESENT)
        )
    }

    // Снять регистрацию
    private fun unRegReceiver(context: Context){
        try {
            context.applicationContext.unregisterReceiver(receiver2)
        } catch (e: Exception) {
            //
        }
    }

    override fun onEnabled(context: Context) {
        regReceiver(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        unRegReceiver(context)
    }


}