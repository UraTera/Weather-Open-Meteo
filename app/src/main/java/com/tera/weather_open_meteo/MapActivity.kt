package com.tera.weather_open_meteo

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.tera.weather_open_meteo.models.CityModel
import com.tera.weather_open_meteo.utils.ConvertDate
import com.tera.weather_open_meteo.utils.DialogManager
import com.tera.weather_open_meteo.utils.MyConst
import com.tera.weather_open_meteo.utils.MyLocation
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import kotlin.concurrent.thread

class MapActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var mapMarker: Marker
    private var city: String? = null
    private var timeZone = ""
    private var region = ""
    private var latitude = 0.0
    private var longitude = 0.0
    private val gson = Gson()
    private val color = MyConst.COLOR_BAR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(color, color),
            navigationBarStyle = SystemBarStyle.light(color, color)
        )
        Configuration.getInstance().load(
            this,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        )
        setContentView(R.layout.activity_map)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightNavigationBars = false

        latitude = intent.getDoubleExtra(MyConst.LATITUDE, 0.0)
        longitude = intent.getDoubleExtra(MyConst.LONGITUDE, 0.0)

        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)

        initMarket()
        initMap()
    }

    // Инициализация маркера
    private fun initMarket() {
        val startPoint = GeoPoint(latitude, longitude)
        val controller = map.controller
        controller.setZoom(10.0)
        controller.setCenter(startPoint)

        mapMarker = Marker(map)
        mapMarker.position = startPoint
        mapMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapMarker.title = "Выбранное место"
        map.overlays.add(mapMarker)
    }

    // Слушатель нажатий с помощью MapEventsOverlay
    private fun initMap() {
        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                if (p != null) {
                    mapMarker.position = p
                    map.controller.setCenter(p)
                    map.invalidate()
                    latitude = p.latitude
                    longitude = p.longitude

                    getCity()
                }
                return true
            }
            override fun longPressHelper(p: GeoPoint?): Boolean = false
        })
        map.overlays.add(mapEventsOverlay)
    }

    private fun getCity() {
        thread {
            city = MyLocation.getCity(this, latitude, longitude)
            val name: String
            if (city != null)
                name = city!!
            else {
                var lat = "%.3f".format(latitude)
                lat = lat.replace(',', '.')
                var long = "%.3f".format(longitude)
                long = long.replace(',', '.')
                name = "$lat, $long"
            }
            runOnUiThread {
                dialogMessage(name)
            }
        }
    }

    private fun sendToMain(name: String) {
        thread {
            region = MyLocation.getRegion(this, latitude, longitude)
            timeZone = MyLocation.getTimeZone(this, latitude, longitude)
            val date = ConvertDate.dateTimeZone(timeZone)

            val list =
                CityModel(name, region, date, latitude, longitude, timeZone, "", 0)

            val intent = Intent()
            val listStr = gson.toJson(list)
            intent.putExtra(MyConst.LIST_CITY, listStr)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun dialogMessage(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
            .setPositiveButton(" ") { _, _ ->
            }
            .setNegativeButton("OK") { _,_ ->
                sendToMain(message)
            }
        val dialog = builder.create()
        val rounded = DialogManager.getRounded()
        dialog.window!!.setBackgroundDrawable(rounded)
        dialog.show()
        dialog.window!!.setLayout(650, 350)
    }

}