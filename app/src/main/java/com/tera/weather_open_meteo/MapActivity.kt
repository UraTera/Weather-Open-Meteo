package com.tera.weather_open_meteo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.skydoves.balloon.balloon
import com.tera.weather_open_meteo.city.BalloonFactory
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
    private lateinit var viewCenter: View
    private val balloon by balloon<BalloonFactory>()
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

        // Проверить подключение к Интернет
        if (!MyLocation.isOnline(this)) {
            DialogManager.messageNoNet(this)
        }

        latitude = intent.getDoubleExtra(MyConst.LATITUDE, 0.0)
        longitude = intent.getDoubleExtra(MyConst.LONGITUDE, 0.0)

        viewCenter = findViewById(R.id.viewCenter)
        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)

        val imBack: ImageView = findViewById(R.id.imBack)
        imBack.setOnClickListener {
            finish()
        }

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

                    getAddress()
                }
                return true
            }
            override fun longPressHelper(p: GeoPoint?): Boolean = false
        })
        map.overlays.add(mapEventsOverlay)
    }

    private fun getAddress() {
        thread {
            val list = MyLocation.getAddress(this, latitude, longitude)
            val city = list[0]
            val region = list[1]

            if (city.isNotEmpty())
                runOnUiThread {
                    showBalloon(city, region)
                }
        }
    }

    private fun showBalloon(city: String, region: String){
        val button: Button = balloon.getContentView().findViewById(R.id.bnBallon)
        val tvCity: TextView = balloon.getContentView().findViewById(R.id.tvCity)
        val tvRegion: TextView = balloon.getContentView().findViewById(R.id.tvRegion)
        tvCity.text = city
        tvRegion.text = region
        balloon.showAlignTop(viewCenter)
        button.setOnClickListener {
            balloon.dismiss()
            sendToMain(city)
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



}