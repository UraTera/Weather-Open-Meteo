package com.tera.weather_open_meteo

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.tera.weather_open_meteo.databinding.ActivitySettingsBinding
import com.tera.weather_open_meteo.utils.MyConst

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private var numTemp = 0
    private var numPress = 0
    private var numWind = 0
    private var numPeriod = 0
    private var period = 0
    private var numFont = 0
    private var keyUpdate = false
    private lateinit var sp: SharedPreferences
    private val color = MyConst.COLOR_BAR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(color, color),
            navigationBarStyle = SystemBarStyle.light(color, color)
        )
        binding = ActivitySettingsBinding.inflate(layoutInflater)
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

        sp = getSharedPreferences(MyConst.SETTING, MODE_PRIVATE)
        numTemp = sp.getInt(MyConst.NUM_TEMP, 0)
        numPress = sp.getInt(MyConst.NUM_PRESS, 0)
        numWind = sp.getInt(MyConst.NUM_WIND, 0)
        numPeriod = sp.getInt(MyConst.NUM_PERIOD, 2)
        numFont = sp.getInt(MyConst.NUM_FONT, 0)
        setParams()

        initButtons()
        initSpinnerPeriod()
        initSpinnerFont()

        val add = 50
        Handler(Looper.getMainLooper()).postDelayed({
            val w = binding.spFont.width
            binding.spPeriod.updateLayoutParams { width = w + add}
            binding.spFont.updateLayoutParams { width = w + add}
        }, 100)

        // Кнопка Back
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    // Кнопки
    private fun initButtons() = with(binding) {
        toolbar.setNavigationOnClickListener {
            openHome()
        }
        rgTemp.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbC -> numTemp = 0
                R.id.rbF -> numTemp = 1
            }
            keyUpdate = true
        }
        rgPress.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbHPa -> numPress = 0
                R.id.rbMBar -> numPress = 1
                R.id.rbMmHg -> numPress = 2
            }
            keyUpdate = true
        }
        rgWind.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbKmHour -> numWind = 0
                R.id.rbMSec -> numWind = 1
                R.id.rbMiHour -> numWind = 2
            }
            keyUpdate = true
        }
    }

    private fun setParams() = with(binding) {
        when (numTemp) {
            0 -> rbC.isChecked = true
            1 -> rbF.isChecked = true
        }
        when (numPress) {
            0 -> rbHPa.isChecked = true
            1 -> rbMBar.isChecked = true
            2 -> rbMmHg.isChecked = true
        }
        when (numWind) {
            0 -> rbKmHour.isChecked = true
            1 -> rbMSec.isChecked = true
            2 -> rbMiHour.isChecked = true
        }
        setFontClock()
    }

    // Список периодов
    private fun initSpinnerPeriod() = with(binding) {
        val fontArray = resources.getStringArray(R.array.period_array)
        val adapter = ArrayAdapter(this@SettingsActivity, R.layout.item_spinner, fontArray)

        spPeriod.adapter = adapter
        spPeriod.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                numPeriod = p2
                setPeriod()
                //Log.d("myLogs", "numPeriod: $numPeriod")
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { }
        }
        spPeriod.setSelection(numPeriod)
    }

    private fun setPeriod() {
        period = when(numPeriod) {
            0 -> 15
            1 -> 30
            2 -> 60
            3 -> 180 // 3 часа
            4 -> 360 // 6 часов
            5 -> 720 // 12 часов
            else -> 1440 // 24 часа
        }
    }

    // Список шрифта
    private fun initSpinnerFont() = with(binding) {
        val fontArray = resources.getStringArray(R.array.font_array)
        val adapter = ArrayAdapter(this@SettingsActivity, R.layout.item_spinner, fontArray)

        spFont.adapter = adapter
        spFont.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                numFont = p2
                setFontClock()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { }
        }

        spFont.setSelection(numFont)
        setFontClock()
    }

    private fun setFontClock() = with(binding){
        val font4 = resources.getFont(R.font.led_bold)
        val font5 = resources.getFont(R.font.led_bold_italic)
        when(numFont) {
            0 -> tcTime.setTypeface(null, Typeface.NORMAL)
            1 -> tcTime.setTypeface(null, Typeface.ITALIC)
            2 -> tcTime.setTypeface(null, Typeface.BOLD)
            3 -> tcTime.setTypeface(null, Typeface.BOLD_ITALIC)
            4 -> tcTime.typeface = font4
            5 -> tcTime.typeface = font5
        }
    }

    private fun openHome() {
        val intent = Intent()
        intent.putExtra(MyConst.KEY_UPDATE, keyUpdate)
        intent.putExtra(MyConst.NUM_FONT, numFont)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onPause() {
        super.onPause()
        sp.edit {
            putInt(MyConst.NUM_TEMP, numTemp)
            putInt(MyConst.NUM_PRESS, numPress)
            putInt(MyConst.NUM_WIND, numWind)
            putInt(MyConst.NUM_PERIOD, numPeriod)
            putInt(MyConst.PERIOD, period)
            putInt(MyConst.NUM_FONT, numFont)
        }
    }

    // Кнопка Back
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            openHome()
        }
    }

}