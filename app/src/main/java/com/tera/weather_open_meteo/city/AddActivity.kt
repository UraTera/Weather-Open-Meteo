package com.tera.weather_open_meteo.city

import android.content.Intent
import android.content.res.Resources
import android.location.Geocoder
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.google.gson.Gson
import com.tera.progress.ProgressBarAnim
import com.tera.weather_open_meteo.R
import com.tera.weather_open_meteo.models.CityModel
import com.tera.weather_open_meteo.utils.ConvertDate
import com.tera.weather_open_meteo.utils.DialogManager
import com.tera.weather_open_meteo.utils.MyConst
import com.tera.weather_open_meteo.utils.MyLocation
import java.io.IOException
import kotlin.concurrent.thread

class AddActivity : AppCompatActivity() {

    private lateinit var edSearch: EditText
    private lateinit var lineSearch: LinearLayout
    private lateinit var progress: ProgressBarAnim
    private lateinit var imBack: ImageView
    private var keyNen = false
    private val gson = Gson()
    private val color = MyConst.COLOR_BAR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(color, color),
            navigationBarStyle = SystemBarStyle.light(color, color)
        )
        setContentView(R.layout.activity_add)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightNavigationBars = false

        edSearch = findViewById(R.id.edSearch)
        lineSearch = findViewById(R.id.lineSearch)
        progress = findViewById(R.id.progress)

        imBack = findViewById(R.id.imBack)
        imBack.setOnClickListener {
            finish()
        }

        setProgressBar(false)

        // Проверить подключение к Интернет
        if (!MyLocation.isOnline(this)) {
            keyNen = true
            DialogManager.messageNoNet(this)
        }

        initEditText()
    }

    private fun initEditText() {

        edSearch.doOnTextChanged { text, _, _, _ ->

            val addressSearch = text.toString()

            if (addressSearch.isEmpty()) {
                setProgressBar(false)
                lineSearch.removeAllViews()
                return@doOnTextChanged
            }

            if (keyNen) {
                DialogManager.messageNoNet(this)
                return@doOnTextChanged
            } else
                setProgressBar(true)

            @Suppress("DEPRECATION")
            thread {
                try {
                    val geocoder = Geocoder(this)
                    val addresses = geocoder.getFromLocationName(addressSearch, 30) ?: listOf()

                    if (addressSearch != edSearch.text.toString()) {
                        return@thread
                    }

                    val labels = ArrayList<String>()
                    val listCity = ArrayList<CityModel>()

                    if (addresses.isEmpty()) {
                        return@thread
                    }

                    for (i in addresses.indices) {
                        val address = addresses[i]
                        val addressLines = address.getAddressLine(0)
                        val name = address.locality
                        val latitude = address.latitude
                        val longitude = address.longitude
                        val timeZone = address.timezone(this@AddActivity)
                        val date = ConvertDate.dateTimeZone(timeZone)
                        var region = addressLines.substringAfter(',')
                        region = region.trim()

                        labels.add(addressLines)

                        if (name == null)
                            return@thread

                        val itemCity =
                            CityModel(name, region, date, latitude, longitude, timeZone, "", 0)

                        listCity.add(itemCity)
                    }

                    runOnUiThread {
                        setProgressBar(false)
                        setSuggestions(listCity, labels)
                    }

                } catch (ignore: IOException) {
                }
            }
        }
    }

    // Показать предложения
    private fun setSuggestions(list: ArrayList<CityModel>, labels: List<String>) {
        lineSearch.removeAllViews()
        if (labels.isEmpty()) return

        for (i in list.indices) {
            val label = TextView(this)
            label.text = labels[i]
            label.setPadding(0, dpToPx(), 0, dpToPx())
            label.textSize = 20f
            lineSearch.addView(label)

            label.setOnClickListener {
                val intent = Intent()
                val listStr = gson.toJson(list[i])
                intent.putExtra(MyConst.LIST_CITY, listStr)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun setProgressBar(key: Boolean) {
        if (key) {
            progress.isVisible = true
            progress.animation = true
        } else {
            progress.isVisible = false
            progress.animation = false
        }
    }

    private fun dpToPx(): Int {
        val dp = 8.0
        return (dp * Resources.getSystem().displayMetrics.density + 0.5).toInt()
    }

}