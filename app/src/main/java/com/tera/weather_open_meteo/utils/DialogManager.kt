package com.tera.weather_open_meteo.utils

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import com.tera.weather_open_meteo.R

object DialogManager {

    fun locationSettingsDialog(context: Context, listener: Listener){
        val title = context.getString(R.string.no_gps)
        val message = context.getString(R.string.enable_gps)
        val icon = R.drawable.ic_warning
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        dialog.setIcon(icon)
        dialog.setTitle(title)
        dialog.setMessage(message)
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK"){ _,_ ->
            listener.onClick(null)
            dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена"){ _,_ ->
            dialog.dismiss()
        }
        dialog.show()
    }

    interface Listener{
        fun onClick(name: String?)
    }

    fun messageDialog(context: Context, message: String){
        val icon = R.drawable.ic_history_blue
        val title = context.getString(R.string.updates)
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.setIcon(icon)
        dialog.setTitle(title)
        dialog.setMessage(message)
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK"){ _,_ ->
            dialog.dismiss()
        }

        dialog.show()
    }
}