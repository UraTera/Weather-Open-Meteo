package com.tera.weather_open_meteo.city

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tera.weather_open_meteo.MainActivity
import com.tera.weather_open_meteo.MapActivity
import com.tera.weather_open_meteo.R
import com.tera.weather_open_meteo.adapters.CityAdapter
import com.tera.weather_open_meteo.adapters.ItemClickListener
import com.tera.weather_open_meteo.databinding.ActivityListBinding
import com.tera.weather_open_meteo.models.CityModel
import com.tera.weather_open_meteo.utils.ConvertDate
import com.tera.weather_open_meteo.utils.DialogManager
import com.tera.weather_open_meteo.utils.MyConst
import com.tera.weather_open_meteo.utils.ORANGE

class ListActivity : AppCompatActivity(), ItemClickListener {

    private lateinit var binding: ActivityListBinding

    private var launcherAdd: ActivityResultLauncher<Intent>? = null
    private var launcherMap: ActivityResultLauncher<Intent>? = null

    private lateinit var sp: SharedPreferences
    private val gson = Gson()
    private var listCity = ArrayList<CityModel>()
    private var listSelect = ArrayList<Int>()
    private var latitude = 0.0
    private var longitude = 0.0
    private var timeLastUpdate = 0L
    private var keyCheck = false
    private var keyPeriod = false
    private var keyUpdate = false
    private val color = MyConst.COLOR_BAR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.light(color, color))
        binding = ActivityListBinding.inflate(layoutInflater)
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

        val listStr = intent.getStringExtra(MyConst.LIST_HOME)
        val listHome = gson.fromJson(listStr, CityModel::class.java)
        listCity.add(listHome)

        latitude = listHome.latitude
        longitude = listHome.longitude

        restore()

        // Обновить дату
        for (i in listCity.indices) {
            val timeZone = listCity[i].timeZone
            val date = ConvertDate.dateTimeZone(timeZone)
            listCity[i].date = date
        }

        val timeNew = ConvertDate.currentTimeMillis()
        val timeDiff = (timeNew - timeLastUpdate) / 60000f

        if (timeDiff > MyConst.PERIOD_UPDATE || keyPeriod) {
            getWeather()
        }

//        getWeather()
        setAdapter(this, listCity)

        initButtons()

        launcherAdd =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    val intent = result.data
                    if (intent != null) {
                        val cityStr = intent.getStringExtra(MyConst.LIST_CITY).toString()
                        val list = gson.fromJson(cityStr, CityModel::class.java)
                        listCity.add(list)

                        setAdapter(this, listCity)
                        getWeather()
                    }
                }
            }

        launcherMap =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == RESULT_OK) {
                    val intent = result.data
                    if (intent != null) {
                        val cityStr = intent.getStringExtra(MyConst.LIST_CITY).toString()
                        val list = gson.fromJson(cityStr, CityModel::class.java)
                        listCity.add(list)

                        setAdapter(this, listCity)
                        getWeather()
                    }
                }
            }

        setOrientation()
        setBackground()

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun restore() {
        val listStr = sp.getString(MyConst.LIST_CITY, "[]")
        val type = object : TypeToken<ArrayList<CityModel>>() {}.type
        val list = gson.fromJson<ArrayList<CityModel>>(listStr, type)

        if (list.isNotEmpty()) {
            for (i in list.indices) {
                listCity.add(list[i])
            }
        }

        timeLastUpdate = sp.getLong(MyConst.TIME_LAST, 0L)
        keyPeriod = sp.getBoolean(MyConst.KEY_PERIOD, false)
    }

    private fun getWeather() {
        for (i in listCity.indices) {
            WeatherCity().getWeatherTemp(this, listCity, i)
        }
        timeLastUpdate = ConvertDate.currentTimeMillis()
    }

    fun setWeather(context: Context, list: ArrayList<CityModel>) {
        listCity = list
        setAdapter(context, listCity)
    }

    private fun setAdapter(context: Context, list: ArrayList<CityModel>) {
        val view = context as Activity
        val rcCity = view.findViewById<RecyclerView>(R.id.rcCity)
        val adapter = CityAdapter(list, keyCheck, this)
        rcCity.adapter = adapter
    }

    override fun onItemClick(context: Context, position: Int) {
        sendToMain(context, position)
    }

    override fun onItemClickDelete(list: ArrayList<Int>) {
        ORANGE.listSelect = list
        ORANGE.keyUpdate = list.isNotEmpty()
    }

    private fun sendToMain(context: Context, pos: Int) {
        val list = listCity[pos]
        val listStr = gson.toJson(list)
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(MyConst.LIST_CITY, listStr)
        intent.putExtra(MyConst.KEY_LIST, true)
        intent.putExtra(MyConst.CITY_POS, pos)
        context.startActivity(intent)
    }

    // Кнопки
    private fun initButtons() = with(binding) {

        tvSearch.setOnClickListener {
            val intent = Intent(this@ListActivity, MapActivity::class.java)
            intent.putExtra(MyConst.LATITUDE, latitude)
            intent.putExtra(MyConst.LONGITUDE, longitude)
            launcherMap?.launch(intent)
        }

        imAdd.setOnClickListener {
            val intent = Intent(this@ListActivity, AddActivity::class.java)
            launcherAdd?.launch(intent)
        }

        imClear.setOnClickListener {
            listSelect = ORANGE.listSelect

            if (listSelect.isNotEmpty()) {
                dialogDelete()
            } else {
                val size = listCity.size
                if (size == 1) {
                    val city = listCity[0].name
                    dialogMessage(city)
                } else
                    dialogClean()
            }
        }

    }

    // Сообщение
    private fun dialogMessage(name: String) {
        val icon = R.drawable.ic_warning_yellow
        val title = getString(R.string.remove)
        val message = getString(R.string.remove_no, name)
        DialogManager.dialogMessage(this, icon, title, message)
    }

    // Диалог выбранных элементов
    private fun dialogDelete() {
        val title = getString(R.string.remove)
        val message = getString(R.string.remove_sel)
        val cancel = getString(R.string.cancel)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setIcon(R.drawable.ic_warning_red)
            .setPositiveButton("OK") { _, _ ->
                deleteItems()
                clearSelect()
            }
            .setNegativeButton(cancel) { _, _ ->
                clearSelect()
            }
        builder.setCancelable(false)
        val dialog = builder.create()
        val rounded = DialogManager.getRounded()
        dialog.window!!.setBackgroundDrawable(rounded)
        dialog.show()
    }

    // Удалить позиции
    private fun deleteItems() {
        for (i in listSelect.reversed()) {
            listCity.removeAt(i)
        }
    }

    // Передать в адаптер обновленный список. Остальное в исходное состояние.
    private fun clearSelect() {
        keyCheck = true
        setAdapter(this, listCity)
        listSelect.clear()
        ORANGE.keyUpdate = false
    }

    private fun dialogClean() {
        val title = getString(R.string.remove)
        val message = getString(R.string.remove_all)
        val cancel = getString(R.string.cancel)

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setIcon(R.drawable.ic_warning_red)
            .setPositiveButton("OK") { _, _ ->
                allClean()
            }
            .setNegativeButton(cancel) { _, _ ->
            }
        builder.setCancelable(false)
        val dialog = builder.create()
        val rounded = DialogManager.getRounded()
        dialog.window!!.setBackgroundDrawable(rounded)
        dialog.show()
    }

    // Очистить весь список и передать в адаптер.
    private fun allClean() {
        val list0 = listCity[0]
        listCity.clear()
        listCity.add(list0)
        val listStr = gson.toJson("[]")

        sp.edit {
            putString(MyConst.LIST_CITY, listStr)
        }
        setAdapter(this, listCity)
    }

    override fun onPause() {
        super.onPause()
        val list = listCity.subList(1, listCity.size)
        val listStr = gson.toJson(list)
        sp.edit {
            putString(MyConst.LIST_CITY, listStr)
            putLong(MyConst.TIME_LAST, timeLastUpdate)
        }
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
            binding.main.setBackgroundResource(R.drawable.map_v)
        else
            binding.main.setBackgroundResource(R.drawable.map_h)
    }

    // Кнопка Back
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            keyUpdate = ORANGE.keyUpdate
            if (keyUpdate) {
                clearSelect()
            } else
                finish()
        }
    }


}