package com.fantasyfang.materialluckywheel

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.withTranslation
import com.fantasyfang.materialluckywheel.model.LuckyItem
import com.fantasyfang.materialluckywheel.model.Vector

class MaterialLuckyWheelView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val radius: Float = 400f
    private lateinit var itemList: List<LuckyItem>

    //Paint
    private val arcPaint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 10f
    }
    private val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 14f,
            resources.displayMetrics
        )
    }
    //Custom property

    private val outsideRectF = RectF(-radius, -radius, radius, radius)

    fun setItemList(itemList: List<LuckyItem>) {
        this.itemList = itemList
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val canvasWidth = canvas.width
        val canvasHeight = canvas.height
        val centerVector = Vector(canvasWidth * 0.5f, canvasHeight * 0.5f)
        val sweepAngle = 360f / itemList.size

        //1. Set central
        canvas.withTranslation(centerVector.x, centerVector.y) {
            itemList.forEachIndexed { index, luckyItem ->
                //2.1 Draw content of pie color
                arcPaint.color = ContextCompat.getColor(context, luckyItem.backgroundColor)
                arcPaint.style = Paint.Style.FILL_AND_STROKE
                drawTargetArc(canvas, index, sweepAngle)

                //2.2 Draw border of pie color
                arcPaint.color = ContextCompat.getColor(context, android.R.color.white)
                arcPaint.style = Paint.Style.STROKE
                drawTargetArc(canvas, index, sweepAngle)

                //3 Draw text
                drawTargetText(canvas, index, sweepAngle, luckyItem.text, luckyItem.backgroundColor)
            }
        }
    }

    private fun drawTargetText(
        canvas: Canvas,
        index: Int,
        sweepAngle: Float,
        text: String,
        @ColorRes backgroundColor: Int
    ) {
        val path = Path()
        path.addArc(outsideRectF, index * sweepAngle, sweepAngle)

        val textWidth: Float = textPaint.measureText(text)
        val hOffset = (radius * Math.PI / itemList.size - textWidth / 2).toFloat()
        val vOffset = 60f//TODO: Use topTextPadding

        canvas.drawTextOnPath(text, path, hOffset, vOffset, textPaint)
    }

    private fun drawTargetArc(canvas: Canvas, index: Int, sweepAngle: Float) {
        canvas.drawArc(
            outsideRectF,
            index * sweepAngle,
            sweepAngle,
            true,
            arcPaint
        )
    }

    private fun demoDrawArc(canvas: Canvas) {
        canvas.drawArc(outsideRectF, 0f, 120f, true, arcPaint)
        canvas.drawArc(outsideRectF, 120f, 120f, true, arcPaint)
        canvas.drawArc(outsideRectF, 240f, 120f, true, arcPaint)
    }

}