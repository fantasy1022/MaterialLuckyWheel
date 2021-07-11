package com.fantasyfang.materialluckywheel

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.withTranslation
import com.fantasyfang.library.R
import com.fantasyfang.materialluckywheel.extension.withTranslationCustom
import com.fantasyfang.materialluckywheel.model.Vector
import java.lang.Math.cos
import java.lang.Math.sin

class CursorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var radius = 80f
    private val degToPi = Math.PI / 180
    private val circlePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(context, R.color.purple_2001) // TODO: set color
    }
    private val linePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = ContextCompat.getColor(context, R.color.purple_2001) // TODO: set color
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // TODO: set size Depend on lucky view size
        // set radius
    }

    override fun onDraw(canvas: Canvas) {
        // TODO: draw cursor and fill color
        val canvasWidth = canvas.width
        val canvasHeight = canvas.height
        val centerVector = Vector(canvasWidth * 0.5f, canvasHeight * 0.5f)

        canvas.withTranslationCustom(centerVector.x, centerVector.y) {
            drawCenterCircle()
            drawRightLine()
        }
    }

    private fun Canvas.drawCenterCircle() {
        drawCircle(0f, 0f, radius, circlePaint)
    }

    private fun Canvas.drawRightLine() {
        val startVector = getVector(330)
        val endVector = getVector(270)

        drawLine(
            (startVector.x * radius).apply {
                Log.d("Fan", "startX:$this")
            },
            (startVector.y * radius).apply {
                Log.d("Fan", "startY:$this")
            },
            endVector.x * radius * 2.apply {
                Log.d("Fan", "endX:$this")
            },
            endVector.y * radius * 2.apply {
                Log.d("Fan", "endY:$this")
            },
            linePaint
        )
    }

    private fun getVector(degree: Int): Vector {
        val value = degree * degToPi
        Log.d("Fan", "value:$value")
        return Vector(
            kotlin.math.cos(value).toFloat().apply {
                Log.d("Fan", "cos value:$this")
            },
            kotlin.math.sin(value).toFloat()
        ).apply {
            Log.d("Fan", "sin value:$this")
        }
    }
}
