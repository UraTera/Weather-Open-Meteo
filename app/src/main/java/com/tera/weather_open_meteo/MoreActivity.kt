package com.tera.weather_open_meteo

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.tera.weather_open_meteo.databinding.ActivityMoreBinding
import com.tera.weather_open_meteo.utils.ConvertDate
import com.tera.weather_open_meteo.utils.MyConst
import com.tera.weather_open_meteo.utils.ORANGE

class MoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoreBinding
    private val color = MyConst.COLOR_BAR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.Companion.light(color, color))
        binding = ActivityMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightNavigationBars = false

        val city = ORANGE.city
        binding.tvCity.text = city

        setChart()
        initControl()
        setOrientation()
        setBackground()

        Handler(Looper.getMainLooper()).postDelayed({
            setPosText()
        }, 200)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun initControl() = with(binding){
        scroll1.setOnScrollChangeListener { p0, x, y, p3, p4 ->
            scroll2.scrollTo(x, y)
        }
        scroll2.setOnScrollChangeListener { p0, x, y, p3, p4 ->
            scroll1.scrollTo(x, y)
        }
        imBack.setOnClickListener {
            sendToMain()
        }
    }

    private fun setPosText() = with(binding) {
        val marginTop = resources.getDimension(R.dimen.margin_top)
        val chartHeight = tempChart.chartHeight

        // Давление
        var margin = (marginTop + chartHeight).toInt()
        var param = tvPress.layoutParams as ViewGroup.MarginLayoutParams
        param.topMargin = margin
        tvPress.layoutParams = param

        margin += chartHeight.toInt() // Скорость ветра
        param = tvWind.layoutParams as ViewGroup.MarginLayoutParams
        param.topMargin = margin
        tvWind.layoutParams = param

        margin += chartHeight.toInt() // Осадки
        param = tvFall.layoutParams as ViewGroup.MarginLayoutParams
        param.topMargin = margin
        tvFall.layoutParams = param

        margin += chartHeight.toInt() // Облачность
        param = tvClouds.layoutParams as ViewGroup.MarginLayoutParams
        param.topMargin = margin
        tvClouds.layoutParams = param

        margin += chartHeight.toInt() // Влажность
        param = tvHumidity.layoutParams as ViewGroup.MarginLayoutParams
        param.topMargin = margin
        tvHumidity.layoutParams = param

        margin += chartHeight.toInt() // УФ-индекс
        param = tvUV.layoutParams as ViewGroup.MarginLayoutParams
        param.topMargin = margin
        tvUV.layoutParams = param
    }

    private fun setChart() = with(binding){
        val arrayTime = ORANGE.arrayTime
        if (arrayTime.isEmpty()) return@with

        topChart.dataValueString = ORANGE.arrayTemp
        topChart.dataAxisString = arrayTime
        topChart.icons = ORANGE.arrayIcon

        tempChart.dataValueString = ORANGE.arrayTemp
        tempChart.dataAxisString = arrayTime

        pressChart.dataValueString = ORANGE.arrayPress
        pressChart.dataAxisString = arrayTime

        windChart.dataValueString = ORANGE.arrayWind
        windChart.dataAxisString = arrayTime

        falloutChart.dataValueString = ORANGE.arrayFallout
        falloutChart.dataAxisString = arrayTime

        cloudsChart.dataValueString = ORANGE.arrayClouds
        cloudsChart.dataAxisString = arrayTime

        humidityChart.dataValueString = ORANGE.arrayHumidity
        humidityChart.dataAxisString = arrayTime

        uvChart.dataValueString = ORANGE.arrayUV
        uvChart.dataAxisString = arrayTime

        setText()
    }

    private fun setText() = with(binding) {
        val numPress = ORANGE.numPress
        val numWind = ORANGE.numWind
        val numFall = ORANGE.numFall

        var str1 = getString(R.string.press)
        var str2 = ""
        when (numPress) {
            0 -> str2 = getString(R.string.unit_press_hpa)
            1 -> str2 = getString(R.string.unit_press_mbr)
            2 -> str2 = getString(R.string.unit_press_mmHg)
        }
        var text = "$str1, $str2"
        tvPress.text = text

        str1 = getString(R.string.wind_speed)
        when (numWind) {
            0 -> str2 = getString(R.string.unit_wind_kmh)
            1 -> str2 = getString(R.string.unit_wind_mc)
            2 -> str2 = getString(R.string.unit_wind_mlh)
        }
        text = "$str1, $str2"
        tvWind.text = text

        str1 = getString(R.string.fall)
        when(numFall){
            0 -> str2 = getString(R.string.unit_mm)
            1 -> str2 = getString(R.string.unit_in)
        }
        text = "$str1, $str2"
        tvFall.text = text
    }

    private fun sendToMain() {
        val intent = Intent()
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun setOrientation() {
        val diagonal = ConvertDate.getScreenDiagonal(this)
        if (diagonal < 7)
            this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        else
            this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    private fun setBackground() {
        val screen = resources.displayMetrics
        val w = screen.widthPixels
        val h = screen.heightPixels
        if (w < h)
            binding.main.setBackgroundResource(R.drawable.sky_v)
        else
            binding.main.setBackgroundResource(R.drawable.sky_h)
    }

    // Кнопка Back
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            sendToMain()
        }
    }
}