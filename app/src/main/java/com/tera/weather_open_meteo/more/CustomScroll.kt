package com.tera.weather_open_meteo.more

import android.content.Context
import android.util.AttributeSet
import android.widget.HorizontalScrollView

typealias OnScrollListener = (x: Int, y: Int) -> Unit

class CustomScroll: HorizontalScrollView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    private var mListener: OnScrollListener? = null

    fun setOnScrollListener(listener: OnScrollListener?) {
        mListener = listener
    }

    override fun onScrollChanged(x: Int, y: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(x, y, oldl, oldt)
        mListener?.invoke(x, y)
    }
}