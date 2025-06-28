package com.tera.weather_open_meteo.utils

object MyConst {
    const val URL_API = "https://api.open-meteo.com/v1/forecast?"
    const val WIDGET_ID = "widget_id"
    const val WIDGET_NUM = "widget_num"
    const val SETTING = "settings"
    const val LIST_CURR = "list_curr"
    const val LIST_DAYS = "list_days"
    const val LIST_TEMP = "list_temp"
    const val LIST_TIME = "list_time"
    const val LIST_ICON = "list_icon"
    const val LIST_CITY = "list_city"
    const val LIST_HOME = "list_home"

    const val LATITUDE = "latitude"
    const val LONGITUDE = "longitude"
    const val CITY = "city"
    const val CITY_POS = "city_pos"
    const val REGION = "region"
    const val TIME_ZONE = "time_zone"
    const val TIME_ZONE_NO = "Etc/GMT-2"
    const val TEMP = "temp"
    const val ICON = "icon"

    const val NUM_TEMP = "num_temp"
    const val NUM_PRESS = "num_press"
    const val NUM_PERIOD = "num_period"
    const val PERIOD = "period"
    const val NUM_WIND = "num_wind"
    const val NUM_FONT = "num_font"
    const val TIME_LAST = "time_last"
    const val KEY_UPDATE = "key_update"
    const val KEY_PERIOD = "key_period"
    const val KEY_LIST = "key_list"
    const val PERIOD_UPDATE = 30
    const val PATTERN_FORMAT = "yyyy-MM-dd HH:mm"
    const val DATE_FORMAT = "EEE, dd LLLL H:mm"
    const val HPA_MM_HG = 0.75006375542
    const val KM_MIL = 0.62137
    const val COLOR_BAR = -15522965

    var widgetId = 0
    var widgetNum = 0
    var listSelect = ArrayList<Int>()
    var keyUpdate = false
}