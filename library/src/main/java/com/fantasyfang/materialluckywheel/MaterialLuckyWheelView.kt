package com.fantasyfang.materialluckywheel

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.withTranslation
import com.fantasyfang.materialluckywheel.extension.isColorDark
import com.fantasyfang.materialluckywheel.model.LuckyItem
import com.fantasyfang.materialluckywheel.model.Vector
import kotlin.math.cos
import kotlin.math.sin


class MaterialLuckyWheelView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val radius = 400f
    private val degToPi = Math.PI / 180
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

                //4 Draw icon
                drawImage(
                    canvas, index, sweepAngle,
                    BitmapFactory.decodeResource(resources, luckyItem.icon)
                )
            }
        }
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

    private fun drawTargetText(
        canvas: Canvas, index: Int, sweepAngle: Float, text: String,
        @ColorRes backgroundColor: Int
    ) {
        val path = Path()
        path.addArc(outsideRectF, index * sweepAngle, sweepAngle)

        //if (textColor == 0)
        textPaint.color = if (backgroundColor.isColorDark()) Color.WHITE else Color.BLACK

        val textWidth: Float = textPaint.measureText(text)
        val hOffset = (radius * Math.PI / itemList.size - textWidth / 2).toFloat()
        val vOffset = 80f//TODO: Use topTextPadding

        canvas.drawTextOnPath(text, path, hOffset, vOffset, textPaint)
    }

    private fun drawImage(canvas: Canvas, index: Int, sweepAngle: Float, bitmap: Bitmap) {
        //TODO: check scale, rotate and position logic
        when (index) {
            0 -> {
                val matrix = Matrix().apply {
                    postScale(0.5f, 0.5f)
                    postRotate(135f, bitmap.width.toFloat() / 4, bitmap.height.toFloat() / 4)
                    postTranslate(50f, 50f)
                }
                canvas.drawBitmap(bitmap, matrix, null)
            }
            1 -> {
                val matrix = Matrix().apply {
                    postScale(0.5f, 0.5f)
                    postRotate(225f, bitmap.width.toFloat() / 4, bitmap.height.toFloat() / 4)
                    postTranslate(-250f, 50f)
                }
                canvas.drawBitmap(bitmap, matrix, null)
            }
            2 -> {
                val matrix = Matrix().apply {
                    postScale(0.5f, 0.5f)
                    postRotate(315f, bitmap.width.toFloat() / 4, bitmap.height.toFloat() / 4)
                    postTranslate(-250f, -250f)
                }
                canvas.drawBitmap(bitmap, matrix, null)
            }
            3 -> {
                val matrix = Matrix().apply {
                    postScale(0.5f, 0.5f)
                    postRotate(45f, bitmap.width.toFloat() / 4, bitmap.height.toFloat() / 4)
                    postTranslate(50f, -250f)
                }
                canvas.drawBitmap(bitmap, matrix, null)
            }
        }
    }

    private fun demoDrawArc(canvas: Canvas) {
        canvas.drawArc(outsideRectF, 0f, 120f, true, arcPaint)
        canvas.drawArc(outsideRectF, 120f, 120f, true, arcPaint)
        canvas.drawArc(outsideRectF, 240f, 120f, true, arcPaint)
    }

    private fun demoDrawImage(canvas: Canvas, index: Int, sweepAngle: Float , bitmap: Bitmap) {
        val imgWidth = radius / itemList.size
        val angle = ((index * sweepAngle + 360f / itemList.size / 2) * Math.PI / 180)

        Log.d("Fam", "angle:$angle")
        val x = (radius / 2 * cos(angle))
        val y = (radius / 2 * sin(angle))

        Log.d("Fam", "x:$x")
        Log.d("Fam", "y:$y")
        //TODO: check position
        val rect = Rect(
            (x - imgWidth / 2).toInt(), (y - imgWidth / 2).toInt(),
            (x + imgWidth / 2).toInt(), (y + imgWidth / 2).toInt()
        )
        canvas.drawBitmap(bitmap, null, rect, null)
    }
}