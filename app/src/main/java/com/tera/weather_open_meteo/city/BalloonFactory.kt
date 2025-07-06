package com.tera.weather_open_meteo.city

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.createBalloon
import com.tera.weather_open_meteo.R

class BalloonFactory : Balloon.Factory() {
    override fun create(context: Context, lifecycle: LifecycleOwner?): Balloon {

        val groundColor = ContextCompat.getColor(context, R.color.white)

        return createBalloon(context) {
            setArrowSize(10)
            setArrowOrientation(ArrowOrientation.BOTTOM)
            setIsVisibleArrow(true)
            setArrowPosition(0.5f)
            setWidth(200)
            setCornerRadius(10f)
            setElevation(6)
            setPadding(6)
            setBackgroundColor(groundColor)
            setBalloonAnimation(BalloonAnimation.FADE)
            setIsAttachedInDecor(true)
            setLayout(R.layout.layout_balloon)
            setLifecycleOwner(lifecycleOwner)
        }
    }
}