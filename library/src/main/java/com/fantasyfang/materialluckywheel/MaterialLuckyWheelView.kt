package com.fantasyfang.materialluckywheel

import android.animation.Animator
import android.animation.TimeInterpolator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.withTranslation
import com.fantasyfang.materialluckywheel.extension.getAngleOfIndexTarget
import com.fantasyfang.materialluckywheel.extension.isColorDark
import com.fantasyfang.materialluckywheel.model.LuckyItem
import com.fantasyfang.materialluckywheel.model.Vector
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class MaterialLuckyWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val radius = 400f
    private val degToPi = Math.PI / 180
    private var isRunning = false
    private var viewRotation = 0f
    private var fingerRotation = 0.0
    private var downPressTime = 0L
    private var upPressTime = 0L

    var isTouchEnabled = false
    var touchThreshold = 700
    private lateinit var itemList: List<LuckyItem>
    private var listener: MaterialLuckyWheelViewListener? = null

    interface MaterialLuckyWheelViewListener {
        fun onItemSelected(item: LuckyItem)
    }

    // Paint
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
    // Custom property

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

        // 1. Set central
        canvas.withTranslation(centerVector.x, centerVector.y) {
            itemList.forEachIndexed { index, luckyItem ->
                // 2.1 Draw content of pie color
                arcPaint.color = ContextCompat.getColor(context, luckyItem.backgroundColor)
                arcPaint.style = Paint.Style.FILL_AND_STROKE
                drawTargetArc(canvas, index, sweepAngle)

                // 2.2 Draw border of pie color
                arcPaint.color = ContextCompat.getColor(context, android.R.color.white)
                arcPaint.style = Paint.Style.STROKE
                drawTargetArc(canvas, index, sweepAngle)

                // 3 Draw text
                drawTargetText(canvas, index, sweepAngle, luckyItem.text, luckyItem.backgroundColor)

                // 4 Draw icon
                drawImage(
                    canvas, index, sweepAngle,
                    BitmapFactory.decodeResource(resources, luckyItem.icon)
                )

                // 5 Draw center image
//                drawCircle(0f, 0f, 50f, centerImagePaint)
//                drawText("GO!",0f,0f,textPaint)

                // 6 Draw cursor
//                drawCursor(canvas)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d("Fan", "onTouchEvent:$isRunning, $isTouchEnabled")
        if (isRunning || !isTouchEnabled) {
            return false
        }

        val x = event.x
        val y = event.y
        Log.d("Fan", "onTouchEvent:$x, $$y")

        val xCenter = width / 2.toDouble()
        val yCenter = height / 2.toDouble()
        Log.d("Fan", "onTouchEvent xCenter:$xCenter, $$yCenter")
        val newFingerRotation: Double

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                viewRotation = rotation
                Log.d("Fan", "ACTION_DOWN rotation:$rotation")
                fingerRotation = Math.toDegrees(atan2(x - xCenter, yCenter - y))
                Log.d("Fan", "ACTION_DOWN fingerRotation:$fingerRotation")
                downPressTime = event.eventTime
                Log.d("Fan", "ACTION_DOWN downPressTime:$downPressTime")
                true
            }

            MotionEvent.ACTION_MOVE -> {
                newFingerRotation = Math.toDegrees(atan2(x - xCenter, yCenter - y))
                // TODO: check rotation is consist and view jitter
                rotation = newRotationValue(viewRotation, fingerRotation, newFingerRotation)
                Log.d("Fan", "ACTION_MOVE")
                true
            }
            MotionEvent.ACTION_UP -> {
                newFingerRotation = Math.toDegrees(atan2(x - xCenter, yCenter - y))
                val computedRotation =
                    newRotationValue(viewRotation, fingerRotation, newFingerRotation)
                fingerRotation = newFingerRotation

                upPressTime = event.eventTime
                if (upPressTime - downPressTime > touchThreshold) {
                    // Disregarding the touch since the tap is too slow
                    return true
                }
                // TODO: check rotation
                rotateTo(getFallBackRandomIndex(), RotationDirection.Clockwise)

                Log.d("Fan", "ACTION_UP downPressTime:$downPressTime")
                Log.d("Fan", "ACTION_UP upPressTime:$upPressTime")
                Log.d("Fan", "ACTION_UP interval:${upPressTime - downPressTime}")
                true
            }
            else -> {
                Log.d("Fan", "motion else")
                return super.onTouchEvent(event)
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
        canvas: Canvas,
        index: Int,
        sweepAngle: Float,
        text: String,
        @ColorRes backgroundColor: Int
    ) {
        val path = Path()
        path.addArc(outsideRectF, index * sweepAngle, sweepAngle)

        // if (textColor == 0)
        textPaint.color = if (backgroundColor.isColorDark()) Color.WHITE else Color.BLACK

        val textWidth: Float = textPaint.measureText(text)
        val hOffset = (radius * Math.PI / itemList.size - textWidth / 2).toFloat()
        val vOffset = 80f // TODO: Use topTextPadding

        canvas.drawTextOnPath(text, path, hOffset, vOffset, textPaint)
    }

    private fun drawImage(canvas: Canvas, index: Int, sweepAngle: Float, bitmap: Bitmap) {
        // TODO: check scale, rotate and position logic
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

    fun setMaterialLuckyWheelViewListener(listener: MaterialLuckyWheelViewListener) {
        this.listener = listener
    }

    fun rotateTo(
        index: Int,
        rotationDirection: RotationDirection = RotationDirection.Clockwise,
        durationInMilliSeconds: Long = 5000L,
        rotationDegree: Float = 5040f,
        timeInterpolator: TimeInterpolator = AccelerateInterpolator()
    ) {

        animate()
            .setInterpolator(timeInterpolator)
            .setDuration(durationInMilliSeconds / 2)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    rotation = 0f
                    isRunning = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    rotation = 0f
                    isRunning = false
                    decelerateAnimation(index, durationInMilliSeconds / 2, rotationDegree)
                }

                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            })
            .rotation(rotationDegree * rotationDirection.value)
            .start()
    }

    // Set the last round
    private fun decelerateAnimation(
        index: Int,
        durationInMilliSeconds: Long,
        rotationDegree: Float
    ) {
        val offset = 45f // get from sweep angle
        val targetDegree = index.getAngleOfIndexTarget(itemList.size) + rotationDegree + offset

        animate()
            .setInterpolator(DecelerateInterpolator())
            .setDuration(durationInMilliSeconds)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    isRunning = false
                    listener?.onItemSelected(itemList[index])
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            .rotation(targetDegree)
            .start()
    }

    private fun newRotationValue(
        originalWheelRotation: Float,
        originalFingerRotation: Double,
        newFingerRotation: Double
    ): Float {
        val computationalRotation = newFingerRotation - originalFingerRotation
        return (originalWheelRotation + computationalRotation.toFloat() + 360f) % 360f
    }

    private fun getFallBackRandomIndex(): Int = Random.Default.nextInt(itemList.size)

    private fun demoDrawArc(canvas: Canvas) {
        canvas.drawArc(outsideRectF, 0f, 120f, true, arcPaint)
        canvas.drawArc(outsideRectF, 120f, 120f, true, arcPaint)
        canvas.drawArc(outsideRectF, 240f, 120f, true, arcPaint)
    }

    private fun demoDrawImage(canvas: Canvas, index: Int, sweepAngle: Float, bitmap: Bitmap) {
        val imgWidth = radius / itemList.size
        val angle = ((index * sweepAngle + 360f / itemList.size / 2) * Math.PI / 180)

        Log.d("Fam", "angle:$angle")
        val x = (radius / 2 * cos(angle))
        val y = (radius / 2 * sin(angle))

        Log.d("Fam", "x:$x")
        Log.d("Fam", "y:$y")
        // TODO: check position
        val rect = Rect(
            (x - imgWidth / 2).toInt(), (y - imgWidth / 2).toInt(),
            (x + imgWidth / 2).toInt(), (y + imgWidth / 2).toInt()
        )
        canvas.drawBitmap(bitmap, null, rect, null)
    }

    enum class RotationDirection(val value: Int) {
        Clockwise(1), Counterclockwise(-1)
    }
}
