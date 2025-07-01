package com.tera.weather_open_meteo.utils

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import androidx.core.graphics.toColorInt
import com.tera.weather_open_meteo.R

object DialogManager {

    // Диалог влюченя GPS
    fun locationSettingsDialog(context: Context, listener: Listener) {
        val title = context.getString(R.string.no_gps)
        val message = context.getString(R.string.enable_gps)
        val cancel = context.getString(R.string.cancel)

        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        val rounded = getRounded()
        dialog.window!!.setBackgroundDrawable(rounded)
        dialog.setIcon(R.drawable.ic_warning_red)
        dialog.setTitle(title)
        dialog.setMessage(message)
        dialog.setCancelable(false)
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, _ ->
            listener.onClick()
            dialog.dismiss()
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, cancel) { _, _ ->
            dialog.dismiss()
        }
        dialog.show()
    }

    interface Listener {
        fun onClick()
    }

    fun dialogMessage(context: Context, icon: Int, title: String, message: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
            .setIcon(icon)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
            }
        builder.setCancelable(false)
        val dialog = builder.create()
        val rounded = getRounded()
        dialog.window!!.setBackgroundDrawable(rounded)
        dialog.show()
    }

    fun messageNoNet(context: Context) {
        val icon = R.drawable.ic_no_internet
        val title = context.getString(R.string.warning)
        val message = context.getString(R.string.no_net)
        dialogMessage(context, icon, title, message)
    }

    fun getRounded (): InsetDrawable {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.setColor("#FFFFFF".toColorInt())
        drawable.cornerRadius = 50f
        val insetDrawable = InsetDrawable(drawable, 40)
        return insetDrawable
    }



}