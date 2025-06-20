package com.tera.weather_open_meteo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import kotlin.math.roundToInt

class LineChart(
    context: Context,
    attrs: AttributeSet?,
    defStyleRes: Int
) : View(context, attrs, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?) :
            this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    data class ValuePos(
        val x: Float,
        val y: Float
    )

    companion object {

        const val AXIS_WIDTH = 2f
        const val BOTTOM_COLOR = -13943715
        const val TOP_COLOR = -4939179

        const val HEIGHT_MIN = 150f
        const val ICON_SIZE = 36f

        const val LINE_COLOR = -10689051
        const val LINE_LENGTH = 45f
        const val LINE_WIDTH = 3f

        const val MARK_WIDTH = 4f

        const val TEXT_COLOR = Color.BLACK
        const val TEXT_SIZE = 12f

        const val POINT_COLOR = -4787972
        const val POINT_RADIUS = 5f
    }

    private val mPaintLine = Paint()
    private val mPaintPoint = Paint()
    private val mPaintMark = Paint() // Test
    private val mPaintText = Paint()
    private val mPaintTextAxis = Paint()
    private val mPaintAxis = Paint()
    private val mPaintLabel = Paint()
    private val mPaintFilling = Paint()
    private val mPath = Path()

    private var mArrayDataString: ArrayList<String>? = null
    private var mArrayDataFloat: ArrayList<Float>? = null
    private var mArrayIcons: ArrayList<Int>? = null
    private var mArrayTextAxis: ArrayList<String>? = null
    private var mArrayIntAxis: ArrayList<Int>? = null

    private val mArrayValuePos = ArrayList<ValuePos>()

    private var mIndexColor = 0
    private var mIndexText = 0

    private var mViewWidth = 0 // Ширина CustomChart
    private var mOffsetStart = 0f
    private var mOffsetEnd = 0f

    private var mFailedTop = 0f    // Верхнее поле
    private var mFieldBottom = 0f  // Нижнее поле
    private var mFieldAxisTop = 0f // Текстовое поле оси X
    private var mFieldAxisBottom = 0f
    private var mFieldIconTop = 0f // Текстовое поле icon
    private var mFieldIconBottom = 0f
    private var mFieldText = 0f   // Текстовое поле значений
    private var mFieldLabel = 0f  // Поле метки

    private var mX0 = 0f
    private var mY0 = 0f

    private var mTextOffset = 16f

    private var mChartWidth = 0f  // Ширина диаграммы
    private var mChartHeight = 0f // Высота диаграммы
    private var mChartHeightMin = HEIGHT_MIN // 150f

    private var mMaxValue = 0f
    private var handler = Handler(Looper.getMainLooper())

    // Атрибуты
    private var mArrayColor: IntArray? = null

    private var mAxisColor = 0
    private var mAxisShow = true
    private var mAxisWidth = 0f

    private var mChartShow = true
    private var mColorBot = 0
    private var mColorTop = 0
    private var mFillingShow = false
    private var mFillingStyle = 0

    private var mIconShow = true
    private var mIconSize = 0
    private var mIconTop = true

    private var mLabelColor = 0
    private var mLabelSize = 0f
    private var mLabelText: String? = null

    private var mLineColor = 0
    private var mLineLength = 0 // Длина линии
    private var mLineWidth = 0f
    private var mLineZero = false

    private var mMarkAllHeight = false
    private var mMarkColor = 0
    private var mMarkShow = false
    private var mMarkWidth = 0f

    private var mPointColor = 0
    private var mPointRadius = 0f
    private var mPointShow = false

    private var mTextAxisColor = 0
    private var mTextAxisShow = true
    private var mTextAxisSize = 0f
    private var mTextAxisTop = false

    private var mTextColor = 0
    private var mTextOnLine = true
    private var mTextShow = true
    private var mTextSize = 0f
    private var mTextString = true // Текстовые значения

    init {
        context.withStyledAttributes(attrs, R.styleable.LineChart) {

            val colorsId = getResourceId(R.styleable.LineChart_line_arrayColor, 0)
            if (colorsId != 0)
                mArrayColor = resources.getIntArray(colorsId)

            mAxisColor = getColor(R.styleable.LineChart_line_axisColor, LINE_COLOR)
            mAxisShow = getBoolean(R.styleable.LineChart_line_axisShow, true)
            mAxisWidth = getDimension(R.styleable.LineChart_line_axisWidth, dpToPx(AXIS_WIDTH))

            mChartShow = getBoolean(R.styleable.LineChart_line_chartShow, true)

            mColorBot = getColor(R.styleable.LineChart_line_fillingBottomColor, BOTTOM_COLOR)
            mColorTop = getColor(R.styleable.LineChart_line_fillingTopColor, TOP_COLOR)
            mFillingShow = getBoolean(R.styleable.LineChart_line_fillingShow, false)
            mFillingStyle = getInt(R.styleable.LineChart_line_fillingStyle, 0)

            mIconShow = getBoolean(R.styleable.LineChart_line_iconShow, true)
            mIconSize = getDimension(R.styleable.LineChart_line_iconSize, dpToPx(ICON_SIZE)).toInt()
            mIconTop = getBoolean(R.styleable.LineChart_line_iconTop, true)

            mLabelColor = getColor(R.styleable.LineChart_line_labelColor, TEXT_COLOR)
            mLabelSize = getDimension(R.styleable.LineChart_line_labelSize, spToPx())
            mLabelText = getString(R.styleable.LineChart_line_labelText)

            mLineColor = getColor(R.styleable.LineChart_line_lineColor, LINE_COLOR)
            mLineLength = getDimension(R.styleable.LineChart_line_lineLength, dpToPx(LINE_LENGTH)).toInt()
            mLineWidth = getDimension(R.styleable.LineChart_line_lineWidth, dpToPx(LINE_WIDTH))
            mLineZero = getBoolean(R.styleable.LineChart_line_lineStartZero, false)

            mMarkAllHeight = getBoolean(R.styleable.LineChart_line_markZeroAllHeight, false)
            mMarkColor = getColor(R.styleable.LineChart_line_markZeroColor, LINE_COLOR)
            mMarkShow = getBoolean(R.styleable.LineChart_line_markZeroShow, false)
            mMarkWidth = getDimension(R.styleable.LineChart_line_markZeroWidth, dpToPx(MARK_WIDTH))

            mPointColor = getColor(R.styleable.LineChart_line_pointColor, POINT_COLOR)
            mPointRadius = getDimension(R.styleable.LineChart_line_pointRadius, dpToPx(POINT_RADIUS))
            mPointShow = getBoolean(R.styleable.LineChart_line_pointShow, true)

            mTextAxisColor = getColor(R.styleable.LineChart_line_textAxisColor, TEXT_COLOR)
            mTextAxisSize = getDimension(R.styleable.LineChart_line_textAxisSize, spToPx())
            mTextAxisShow = getBoolean(R.styleable.LineChart_line_textAxisShow, true)
            mTextAxisTop = getBoolean(R.styleable.LineChart_line_textAxisTop, false)

            mTextColor = getColor(R.styleable.LineChart_line_textColor, TEXT_COLOR)
            mTextOnLine = getBoolean(R.styleable.LineChart_line_textOnLine, true)
            mTextSize = getDimension(R.styleable.LineChart_line_textSize, spToPx())
            mTextShow = getBoolean(R.styleable.LineChart_line_textShow, true)
            mTextString = getBoolean(R.styleable.LineChart_line_textString, true)

        }

        mAxisWidth /= 2
        mLineWidth /= 2
        mMarkWidth /= 2
        mPointRadius /= 2

        initPaint()
        initParams()

        if (isInEditMode)
            setEditMode()
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }

    private fun spToPx(): Float {
        val sp = TEXT_SIZE
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
    }

    private fun initPaint() {

        mPaintMark.color = mMarkColor
        mPaintMark.style = Paint.Style.STROKE
        mPaintMark.strokeWidth = mMarkWidth

        mPaintLine.color = mLineColor
        mPaintLine.style = Paint.Style.STROKE
        mPaintLine.strokeWidth = mLineWidth
        mPaintLine.isAntiAlias = true

        mPaintAxis.color = mAxisColor
        mPaintAxis.style = Paint.Style.STROKE
        mPaintAxis.strokeWidth = mAxisWidth
        mPaintAxis.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)

        mPaintText.color = mTextColor
        mPaintText.textSize = mTextSize
        mPaintText.textAlign = Paint.Align.CENTER

        mPaintTextAxis.color = mTextAxisColor
        mPaintTextAxis.textSize = mTextAxisSize
        mPaintTextAxis.textAlign = Paint.Align.CENTER

        mPaintLabel.color = mLabelColor
        mPaintLabel.textSize = mLabelSize

        mPaintPoint.color = mPointColor

        mPaintFilling.color = mColorBot
    }

    private fun initParams() {
        // Ширина диаграммы во весь экран
        val screen = resources.displayMetrics
        mViewWidth = screen.widthPixels

        // Радиус точки
        mPointRadius = if (mPointShow) mPointRadius
        else 0f

        // Поле текста
        mFieldText = if (mTextShow) {
            getFontHeight(mPaintText) + mTextOffset
        } else mPointRadius

        if (!mChartShow)
            mChartHeightMin = mFieldText

        // Поле метки
        if (mLabelText != null)
            mFieldLabel = getFontHeight(mPaintLabel) + mTextOffset
    }

    // Режим отладки
    private fun setEditMode() {
        mArrayDataString = arrayListOf("23°C", "21°C", "22°C", "20°C", "21°C", "19°C")
        mArrayDataFloat = stringToFloat(mArrayDataString!!)

        mArrayTextAxis = arrayListOf("22:00", "23:00", "0:00", "1:00", "2:00", "3:00")
        mArrayIntAxis = stringToInt(mArrayTextAxis!!)

        mArrayIcons = arrayListOf(
            R.drawable.ic_sun_cloud1, R.drawable.ic_moon_clouds2, R.drawable.ic_moon_stars,
            R.drawable.ic_cloud, R.drawable.ic_sun_rain, R.drawable.ic_sun1
        )

        // Поля оси текста X и иконок
        setFieldAxis()
        setFieldIcon()
        mFailedTop = mFieldAxisTop + mFieldIconTop
        mFieldBottom = mFieldAxisBottom + mFieldIconBottom

        val size = mArrayDataString!!.size
        mViewWidth = mLineLength * size
    }

    // Получить высоту текста
    private fun getFontHeight(paint: Paint): Float {
        val fm = paint.fontMetrics
        return fm.descent - fm.ascent
    }

    private fun stringToFloat(arrayStr: ArrayList<String>): ArrayList<Float> {
        val array = ArrayList<Float>()
        for (i in arrayStr.indices) {
            val str = arrayStr[i]
            val digit = str.filter { it.isDigit() }.toFloat()
            array.add(digit)
        }
        return array
    }

    private fun stringToInt(arrayStr: ArrayList<String>): ArrayList<Int> {
        val array = ArrayList<Int>()
        for (i in arrayStr.indices) {
            val str = arrayStr[i]
            val digit = str.filter { it.isDigit() }.toInt()
            array.add(digit)
        }
        return array
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mArrayDataString == null) return

        if (mChartShow) {
            // Заливка
            if (mFillingShow)
                drawFilling(canvas)

            // Линейная диаграмма
            drawLineChart(canvas)

            if (mPointShow)
                drawPoint(canvas)

            if (mAxisShow)
                drawAxisX(canvas)

            if (mLabelText != null)
                drawLabel(canvas)
        }

        if (mTextShow)
            drawTextValue(canvas)

        if (mIconShow)
            drawIcon(canvas)

        if (mTextAxisShow)
            drawTextAxisX(canvas)

        if (mMarkShow)
            drawMarkZero(canvas)
    }

    // Заливка
    private fun drawFilling(canvas: Canvas) {
        val path = Path()

        var x1 = 0f
        var x2 = 0f

        for (i in mArrayValuePos.indices) {
            val y = mArrayValuePos[i].y
            if (i == 0) {
                x1 = mArrayValuePos[i].x
                path.moveTo(x1, y)
            } else {
                x2 = mArrayValuePos[i].x
                path.lineTo(x2, y)
            }
        }

        val y1 = height.toFloat() - 200
        val y2 = height - mFieldBottom

        if (mFillingStyle == 1) {
            val shader = LinearGradient(x1, y2, x1, y1, mColorBot, mColorTop, Shader.TileMode.CLAMP)
            mPaintFilling.setShader(shader)
        }

        path.lineTo(x2, y2)
        path.lineTo(x1, y2)
        canvas.drawPath(path, mPaintFilling)
    }

    // Линейная диаграмма
    private fun drawLineChart(canvas: Canvas) {

        if (mArrayColor != null) {
            val sizeArray = mArrayDataString!!.size

            var x2 = 0f
            var y2 = 0f

            mIndexColor = 0
            for (i in mArrayValuePos.indices) {
                mPaintLine.color = getColor()

                val x1 = mArrayValuePos[i].x
                val y1 = mArrayValuePos[i].y

                if (i < sizeArray - 2) {
                    x2 = mArrayValuePos[i + 1].x
                    y2 = mArrayValuePos[i + 1].y
                }
                canvas.drawLine(x1, y1, x2, y2, mPaintLine)
            }
        } else
            canvas.drawPath(mPath, mPaintLine)
    }

    // Цвет из массива
    private fun getColor(): Int {
        val numColor: Int = mArrayColor!!.size

        if (mIndexColor >= numColor) mIndexColor = 0
        val color: Int = mArrayColor!![mIndexColor]
        mIndexColor++
        return color
    }

    // Точки
    private fun drawPoint(canvas: Canvas) {
        for (i in mArrayValuePos.indices) {
            val x1 = mArrayValuePos[i].x
            val y1 = mArrayValuePos[i].y
            canvas.drawCircle(x1, y1, mPointRadius, mPaintPoint)
        }
    }

    // Текст значений
    private fun drawTextValue(canvas: Canvas) {
        val sizeArray = mArrayDataString!!.size

        val offset = mPointRadius + mTextOffset / 2

        var y = if (!mChartShow)
            mFailedTop + mFieldText * 0.75f
        else
            mMaxValue

        for (i in 0..<sizeArray) {
            val x = mArrayValuePos[i].x
            if (!mTextOnLine && mChartShow)
                y = mArrayValuePos[i].y - offset

            val text = if (mTextString)
                mArrayDataString!![i]
            else
                mArrayDataFloat!![i].roundToInt().toString()

            canvas.drawText(text, x, y, mPaintText)
        }
    }

    // Иконка
    private fun drawIcon(canvas: Canvas) {
        if (mArrayIcons == null) return
        var index = 0
        val numIcon = mArrayIcons!!.size

        val y0: Int
        val y1: Int
        val y2: Int
        val w = mIconSize

        if (!mIconTop) {
            y0 = height - mFieldBottom.toInt() + 20
            y1 = y0
            y2 = y0 + w
        } else {
            y0 = mFailedTop.toInt() - 10
            y1 = y0 - w
            y2 = y0
        }

        for (i in mArrayValuePos.indices) {

            if (index >= numIcon) index = 0
            val icon = mArrayIcons!![index]
            index++

            val drawable = ContextCompat.getDrawable(context, icon) as Drawable

            val xc = mArrayValuePos[i].x.toInt()
            val x1 = xc - w / 2
            val x2 = x1 + w
            drawable.setBounds(x1, y1, x2, y2)
            drawable.draw(canvas)
        }
    }

    // Текст оси X
    private fun drawTextAxisX(canvas: Canvas) {
        if (mArrayTextAxis == null) return

        val y: Float = if (mTextAxisTop)
            mFieldAxisTop * 0.75f
        else
            height.toFloat() - mFieldAxisBottom * 0.3f

        mIndexText = 0

        for (i in mArrayValuePos.indices) {
            val x = mArrayValuePos[i].x
            val text = getTextAxis()
            canvas.drawText(text, x, y, mPaintTextAxis)
        }
    }

    // Текст оси Х из массива
    private fun getTextAxis(): String {
        val numText: Int = mArrayTextAxis!!.size

        if (mIndexText >= numText) mIndexText = 0
        val text = mArrayTextAxis!![mIndexText]
        mIndexText++

        return text
    }

    // Ось X
    private fun drawAxisX(canvas: Canvas) {
        val size = mArrayValuePos.size
        val x1 = mArrayValuePos[0].x
        val x2 = mArrayValuePos[size - 1].x
        val y = height.toFloat() - mFieldBottom //- 10
        canvas.drawLine(x1, y, x2, y, mPaintAxis)

        for (i in mArrayValuePos.indices) {
            val x = mArrayValuePos[i].x
            val y2 = mArrayValuePos[i].y
            canvas.drawLine(x, y, x, y2, mPaintAxis)
        }
    }

    // Метка 0
    private fun drawMarkZero(canvas: Canvas) {

//        Log.d("myLogs", "drawMarkZero")

        if (mArrayIntAxis == null) return

        val indexZero = mArrayIntAxis!!.indexOf(0)

        if (indexZero == -1 || indexZero == 0) return

        val x1 = mArrayValuePos[indexZero].x
        val x2 = mArrayValuePos[indexZero - 1].x
        // Ширина элемента
        val w = x1 - x2
        val x = x1 - w / 2


        val y1: Float
        val y2: Float

        if (!mMarkAllHeight) {
            if (mTextAxisTop) {
                y1 = 0f
                y2 = mFailedTop
            } else {
                y1 = height.toFloat() - mFieldBottom
                y2 = height.toFloat()
            }
        } else {
            y1 = 0f
            y2 = height.toFloat()
        }

        canvas.drawLine(x, y1, x, y2, mPaintMark)

//        Log.d("myLogs", "mArrayIntAxis: $mArrayIntAxis")
//        Log.d("myLogs", "index: $indexZero, x: $x")
    }

    // Label
    private fun drawLabel(canvas: Canvas) {
        val x = mOffsetStart + 30
        val y = mFailedTop + mFieldLabel - 10
        canvas.drawText(mLabelText!!, x, y, mPaintLabel)
    }

    // Параметры диаграмм
    private fun setParams() {
        //Log.d("myLogs", "setParams")

        if (mArrayDataFloat == null) return

        mX0 = mOffsetStart
        mY0 = height - mFieldBottom

        var offsetBottom = 20f // Добавка к диаграмме снизу

        // Ширина элемента
        val size = mArrayDataFloat!!.size
        var w = mChartWidth / size
        var x0 = mOffsetStart + w / 2

        val h = mChartHeight - mFieldText - offsetBottom - mPointRadius - mFieldLabel

        if (mLineZero) { // На всю ширину
            w = (mChartWidth - mPointRadius * 2) / (size - 1)
            x0 = mOffsetStart + mPointRadius
        }
        offsetBottom += mPointRadius

        // Минимальное значение
        val minValue = mArrayDataFloat!!.minOrNull()

        val arrayNew = ArrayList<Float>()

        // Вычесть минимальное значение
        for (i in mArrayDataFloat!!.indices) {
            val y = (mArrayDataFloat!![i] - minValue!!)
            arrayNew.add(y)
        }

        // Максимальное значние
        var maxValue = arrayNew.maxOrNull()
        // Коэффциент по Y
        val k = h / maxValue!!

        // Пересчитать Y
        for (i in arrayNew.indices) {
            val y = arrayNew[i] * k + offsetBottom
            arrayNew[i] = y
        }

        maxValue = arrayNew.maxOrNull()
        mMaxValue = mY0 - maxValue!! - mPointRadius - mTextOffset / 2

        mArrayValuePos.clear()
        mPath.reset()

        // Загрузить список
        for (i in arrayNew.indices) {
            val x = x0 + w * i
            val y = mY0 - arrayNew[i]
            val item = ValuePos(
                x,
                y
            )
            mArrayValuePos.add(item)
            if (i == 0)
                mPath.moveTo(x, y)
            else
                mPath.lineTo(x, y)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val wC = mViewWidth
        val hC = mFailedTop.toInt() + mFieldBottom.toInt() +
                mChartHeightMin.toInt() + mFieldLabel.toInt()
        setMeasuredDimension(
            resolveSize(wC, widthMeasureSpec),
            resolveSize(hC, heightMeasureSpec)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mChartWidth = w.toFloat() - mOffsetStart - mOffsetEnd
        mChartHeight = h.toFloat() - mFailedTop - mFieldBottom
        setParams()
    }

    //------

    var dataValueString: ArrayList<String>? = null
        set(value) {
            field = value
            if (value != null) {
                mArrayDataString = value
                mArrayDataFloat = stringToFloat(mArrayDataString!!)
                setWidth()
            }
        }

    // Размеры диаграммы
    private fun setWidth() {
        val screen = resources.displayMetrics
        val screenWidth = screen.widthPixels
        val size = mArrayDataString?.size

        handler.post {

            if (mLineLength != 0) {
                mViewWidth = mLineLength * size!!
            }

            // Загрузка иконок
            if (mArrayIcons != null) {
                if (mLineLength == 0)
                    mViewWidth = mIconSize * size!!

                setFieldIcon()
            }

            if (mArrayTextAxis != null) {
                setFieldAxis()
                mViewWidth++
            }

            mFailedTop = mFieldAxisTop + mFieldIconTop
            mFieldBottom = mFieldAxisBottom + mFieldIconBottom


            if (mViewWidth < screenWidth)
                mViewWidth = screenWidth

            if (mArrayIcons != null)
                mViewWidth++

            mChartWidth = mViewWidth.toFloat() - mOffsetStart - mOffsetEnd

            requestLayout()
            setParams()
        }
    }

    // Поля текста оси X
    private fun setFieldAxis() {
        if (mTextAxisShow) {
            val textSize = getFontHeight(mPaintTextAxis) + 20
            if (mTextAxisTop)
                mFieldAxisTop = textSize
            else
                mFieldAxisBottom = textSize
        }
    }

    // Поля icons
    private fun setFieldIcon() {
        if (mIconShow) {
            val size = mIconSize.toFloat() + 30
            if (mIconTop)
                mFieldIconTop = size
            else
                mFieldIconBottom = size
        }
    }

    var icons: ArrayList<Int>? = null
        set(value) {
            field = value
            if (value != null) {
                mArrayIcons = value
            }
        }


    var colors: ArrayList<Int>? = null
        set(value) {
            field = value
            if (value != null) {
                mArrayColor = value.toIntArray()
            }
        }

    var dataAxisString: ArrayList<String>? = null
        set(value) {
            field = value
            if (value != null) {
                mArrayTextAxis = value
                mArrayIntAxis = stringToInt(mArrayTextAxis!!)
            }
        }

}