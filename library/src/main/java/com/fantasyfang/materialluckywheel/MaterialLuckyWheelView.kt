package com.fantasyfang.materialluckywheel

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.withTranslation
import com.fantasyfang.materialluckywheel.model.Vector

class MaterialLuckyWheelView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val radius: Float = 300f
    private val mockData: List<Int> = listOf(1, 2, 3)

    //Paint
    private val outsideFramePaint = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.white)
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 10f
        isDither = true
    }

    private val outsideRectF = RectF(-radius, -radius, radius, radius)

    override fun onDraw(canvas: Canvas) {
        val canvasWidth = canvas.width
        val canvasHeight = canvas.height
        val centerVector = Vector(canvasWidth * 0.5f, canvasHeight * 0.5f)


        //1. Set central
        canvas.withTranslation(centerVector.x, centerVector.y) {
            //2. Draw outside arc
            canvas.drawArc(outsideRectF, 0f, 120f, true, outsideFramePaint)
            canvas.drawArc(outsideRectF, 120f, 120f, true, outsideFramePaint)
            canvas.drawArc(outsideRectF, 240f, 120f, true, outsideFramePaint)
        }
    }
}