package com.tera.weather_open_meteo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tera.progress.ProgressBarAnim
import com.tera.weather_open_meteo.adapters.AdapterDays
import com.tera.weather_open_meteo.databinding.ActivityMainBinding
import com.tera.weather_open_meteo.models.CurrentModel
import com.tera.weather_open_meteo.models.DaysModel
import com.tera.weather_open_meteo.utils.ConvertWeather
import com.tera.weather_open_meteo.utils.MyConst
import com.tera.weather_open_meteo.utils.MyLocation
import com.tera.weather_open_meteo.utils.Weather
import com.tera.weather_open_meteo.widgets.Widget
import com.tera.weather_open_meteo.widgets.WidgetTab
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var numFont = 0
    private val gson = Gson()
    private var widgetId = 0
    private var widgetNum = 0
    private var keyUpdate = true
    private var launcherSet: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Разрешение
        setPermissions()

        // Извлекаем ID и номер виджета
        widgetId = intent.getIntExtra(MyConst.WIDGET_ID, 0)
        widgetNum = intent.getIntExtra(MyConst.WIDGET_NUM, 0)

        launcherSet =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    val intent = result.data
                    if (intent != null) {
                        keyUpdate = intent.getBooleanExtra(MyConst.KEY_UPDATE, false)
                        numFont = intent.getIntExtra(MyConst.NUM_FONT, 0)
                        if (keyUpdate) {
                            updateWidget()
                            setFontClock()
                        } else
                            setFontClock()
                    }
                }
            }

        initButtons()
        setOrientation()
        // Кнопка Back
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    // Проверка разрешения.
    private fun setPermissions() {
        if (!MyLocation.isPermissions(this)) {
            MyLocation.permissionListener(this)
        }
    }

    override fun onResume() {
        super.onResume()
        restoreDate()
        if (keyUpdate) {
            setProgressBar(this, 0, 0, true)
            Weather(this).getWeatherMain()
        }
    }

    private fun initButtons() = with(binding) {
        progressBar.animation = false

        progressBar.setOnClickListener {
            //Weather().getWeatherMain(this@MainActivity)
        }
        imSetting.setOnClickListener {
            openSetting()
        }
    }

    // Окно настроек
    private fun openSetting() {
        MyConst.widgetId = widgetId
        MyConst.widgetNum = widgetNum
        val intent = Intent(this, SettingsActivity::class.java)
        launcherSet?.launch(intent)
    }

    private fun updateWidget() {
        val id = MyConst.widgetId
        val num = MyConst.widgetNum
        when (num) {
            1 -> {
                val widget = Widget()
                widget.getWeather(this, id, true)
            }

            2 -> {
                val widget = WidgetTab()
                widget.getWeather(this, id, true)
            }
        }
    }

    // Текущая погода
    fun seCurrentView(context: Context, list: CurrentModel) {
        val view = context as Activity
        view.findViewById<TextView>(R.id.tvCity).text = list.city
        view.findViewById<TextView>(R.id.tvTemp).text = list.currentTemp
        view.findViewById<TextView>(R.id.tvUpdate).text = list.time
        view.findViewById<TextView>(R.id.tvMaxMin).text = list.maxMin
        view.findViewById<TextView>(R.id.tvSunrise).text = list.sunrise
        view.findViewById<TextView>(R.id.tvSunset).text = list.sunset
        view.findViewById<TextView>(R.id.tvCondition).text = list.condition
        view.findViewById<ImageView>(R.id.imWeather).setImageResource(list.icon)
        val tvWeek = view.findViewById<TextView>(R.id.tvWeek)

        val convert = ConvertWeather(context)
        var nameDay = convert.getCurrentTime("EEE.")
        nameDay = nameDay.replaceFirstChar { it.uppercase() }

        tvWeek.text = nameDay

        val calendar = Calendar.getInstance()
        val numDay = calendar.get(Calendar.DAY_OF_WEEK)

        val color = if (numDay == 1 || numDay == 7)
            ContextCompat.getColor(context, R.color.week)
        else
            ContextCompat.getColor(context, R.color.white)

        tvWeek.setTextColor(color)

        //val currentStr = gson.toJson(list).toString()
    }

    // Также из виджетов
    fun saveCurrent(context: Context, list: CurrentModel) {
        val currentStr = gson.toJson(list).toString()
        val sp = context.getSharedPreferences(MyConst.SETTING, Context.MODE_PRIVATE)
        sp.edit {
            putString(MyConst.LIST_CURR, currentStr)
        }
    }

    // Загрузка диаграммы
    fun setChart3(
        context: Context, listTime: ArrayList<String>,
        listTemp: ArrayList<String>, listIcon: ArrayList<Int>,
        keyRestore: Boolean
    ) {
        val view = context as Activity
        val lineChart = view.findViewById<LineChart>(R.id.lineChart)

        lineChart.dataAxisString = listTime
        lineChart.dataValueString = listTemp
        lineChart.icons = listIcon

        if (keyRestore) return

        val timeStr = gson.toJson(listTime)
        val tempStr = gson.toJson(listTemp)
        val iconStr = gson.toJson(listIcon)
        val sp = context.getSharedPreferences(MyConst.SETTING, Context.MODE_PRIVATE)
        sp.edit {
            putString(MyConst.LIST_TIME, timeStr)
            putString(MyConst.LIST_TEMP, tempStr)
            putString(MyConst.LIST_ICON, iconStr)
        }
    }

    // Адаптер прогноза на 6 дней
    fun setAdapterDays(context: Context, list: ArrayList<DaysModel>) {
        val view = context as Activity
        val list2 = list.subList(1, list.size)

        val adapterDays = AdapterDays()
        view.findViewById<RecyclerView>(R.id.rcDays).adapter = adapterDays
        adapterDays.submitList(list2)

        val listStr = gson.toJson(list)
        val sp = context.getSharedPreferences(MyConst.SETTING, Context.MODE_PRIVATE)
        sp.edit {
            putString(MyConst.LIST_DAYS, listStr)
        }
    }

    // Управление прогресс барами
    fun setProgressBar(context: Context, id: Int, num: Int, keyOn: Boolean) {
        //Log.d("myLogs", "setProgressBar 1, num: $num, keyOn: $keyOn")
        when (num) {
            0 -> {
                val view = context as Activity
                view.findViewById<ProgressBarAnim>(R.id.progressBar).animation = keyOn
                //setProgressBarMain(context, keyOn)
            }

            1 -> {
                val widget = Widget()
                widget.setProgress(context, id, keyOn)
            }

            2 -> {
                val widget = WidgetTab()
                widget.setProgress(context, id, keyOn)
            }
        }
    }

    // Установить шрифт
    private fun setFontClock() = with(binding) {
        val font4 = resources.getFont(R.font.led_bold)
        val font5 = resources.getFont(R.font.led_bold_italic)
        when (numFont) {
            0 -> tcTime.setTypeface(null, Typeface.NORMAL)
            1 -> tcTime.setTypeface(null, Typeface.ITALIC)
            2 -> tcTime.setTypeface(null, Typeface.BOLD)
            3 -> tcTime.setTypeface(null, Typeface.BOLD_ITALIC)
            4 -> tcTime.typeface = font4
            5 -> tcTime.typeface = font5
        }
    }

    private fun restoreDate() {
        val sp = getSharedPreferences(MyConst.SETTING, Context.MODE_PRIVATE)
//        sp.edit() {
//            clear()
//        }
        val currentStr = sp.getString(MyConst.LIST_CURR, "")
        val listCurrent = gson.fromJson(currentStr, CurrentModel::class.java)
        val listDaysStr = sp.getString(MyConst.LIST_DAYS, "[]")

        val typeDays = object : TypeToken<ArrayList<DaysModel>>() {}.type
        val listDays = gson.fromJson<ArrayList<DaysModel>>(listDaysStr, typeDays)

        if (listCurrent != null) seCurrentView(this, listCurrent)
        if (listDays.isNotEmpty()) setAdapterDays(this, listDays)

        val timeStr = sp.getString(MyConst.LIST_TIME, "")
        val tempStr = sp.getString(MyConst.LIST_TEMP, "")
        val iconStr = sp.getString(MyConst.LIST_ICON, "")

        val typeStr = object : TypeToken<ArrayList<String>>() {}.type
        val typeInt = object : TypeToken<ArrayList<Int>>() {}.type

        val listTime = gson.fromJson<ArrayList<String>>(timeStr, typeStr)
        val listTemp = gson.fromJson<ArrayList<String>>(tempStr, typeStr)
        val listIcon = gson.fromJson<ArrayList<Int>>(iconStr, typeInt)

        if (listTime != null && listTemp != null && listIcon != null)
            setChart3(this, listTime, listTemp, listIcon, true)

        numFont = sp.getInt(MyConst.NUM_FONT, 0)
        setFontClock()
    }

    override fun onPause() {
        super.onPause()
//        val sp = getSharedPreferences(MyConst.SETTING, Context.MODE_PRIVATE)
//        sp.edit() {
//            clear()
//        }
    }

    private fun setOrientation() {
        val diagonal = MyLocation.getScreenDiagonal(this)
        if (diagonal < 7)
            this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        else
            this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    // Кнопка Back
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finishAffinity()
        }
    }

}