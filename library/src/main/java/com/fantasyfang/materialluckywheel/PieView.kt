package com.fantasyfang.materialluckywheel

import android.animation.Animator
import android.animation.TimeInterpolator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.fantasyfang.materialluckywheel.extension.convertDpToPixel
import com.fantasyfang.materialluckywheel.extension.getAngleOfIndexTarget
import com.fantasyfang.materialluckywheel.extension.isColorDark
import com.fantasyfang.materialluckywheel.extension.withTranslationCustom
import com.fantasyfang.materialluckywheel.model.LuckyItem
import com.fantasyfang.materialluckywheel.model.Vector
import kotlin.math.atan2
import kotlin.random.Random

class PieView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val TAG = PieView::class.java.simpleName

    // Parameter
    private var outerRingColor: Int = 0
    private var outerRingWidth: Int = 0
    private var pieTextSize: Int = 0
    private var pieEdgeWidth: Int = 0
    private var pieEdgeColor: Int = 0
    private var radius = 0f
    private var range = RectF()
    private var padding =
        defaultPaddingDp.convertDpToPixel(context) + defaultBorderWidthDp.convertDpToPixel(context)
    private var isRunning = false
    private var viewRotation = 0f
    private var fingerRotation = 0.0
    private var downPressTime = 0L
    private var upPressTime = 0L
    var isTouchEnabled = false
    private var itemList: List<LuckyItem> = emptyList()
    private var listener: PieViewStateListener? = null

    // Paint
    private val piePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL_AND_STROKE
    }
    private val arcPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }
    private val outerPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }
    private val textPaint = Paint().apply {
        isAntiAlias = true
    }

    // Custom property
    fun setItemList(itemList: List<LuckyItem>) {
        this.itemList = itemList
        invalidate()
    }

    fun setOuterRingColor(outerRingColor: Int) {
        this.outerRingColor = outerRingColor
        outerPaint.color = outerRingColor
        invalidate()
    }

    fun setOuterRingWidth(outerRingWidth: Int) {
        this.outerRingWidth = outerRingWidth
        outerPaint.strokeWidth = outerRingWidth.toFloat()
        invalidate()
    }

    fun setPieTextSize(pieTextSize: Int) {
        this.pieTextSize = pieTextSize
        textPaint.textSize = pieTextSize.toFloat()
        invalidate()
    }

    fun setPieEdgeWidth(pieEdgeWidth: Int) {
        this.pieEdgeWidth = pieEdgeWidth
        arcPaint.strokeWidth = pieEdgeWidth.toFloat()
        invalidate()
    }

    fun setPieEdgeColor(pieEdgeColor: Int) {
        this.pieEdgeColor = pieEdgeColor
        arcPaint.color = pieEdgeColor
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // TODO: 2021/6/7 確認  desired 的邏輯，match_parent, wrap_content 要吃這個值 suggestedMinimumWidth
        val width = Math.min(measuredWidth, measuredHeight)
        Log.d(TAG, "onMeasure: $measuredWidth, $measuredHeight ")

        // mPadding = if (paddingLeft == 0) 10 else paddingLeft
        radius = (width.toFloat() - padding) / 2

        setMeasuredDimension(width, width)
    }

    override fun onDraw(canvas: Canvas) {
        val canvasWidth = canvas.width
        val canvasHeight = canvas.height
        Log.d(TAG, "radius:$radius")

        val centerVector = Vector(canvasWidth * 0.5f, canvasHeight * 0.5f)
        val sweepAngle = 360f / itemList.size
        range = RectF(-radius, -radius, radius, radius)

        // 1. Set central
        canvas.withTranslationCustom(centerVector.x, centerVector.y) {
            itemList.forEachIndexed { index, luckyItem ->
                // 2.1 Draw content of pie color
                piePaint.color = ContextCompat.getColor(context, luckyItem.backgroundColor)
                canvas.drawArc(
                    range,
                    index * sweepAngle,
                    sweepAngle,
                    true,
                    piePaint
                )

                // 2.2 Draw border of pie color
                canvas.drawArc(range, index * sweepAngle, sweepAngle, true, arcPaint)

                // 2.3 Draw circle border
                canvas.drawCircle(0f, 0f, radius, outerPaint)

                // 3 Draw text
                drawTargetText(canvas, index, sweepAngle, luckyItem.text, luckyItem.backgroundColor)

                // 4 Draw icon
                drawImage(
                    canvas, index, sweepAngle,
                    BitmapFactory.decodeResource(resources, luckyItem.icon)
                )
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
            range,
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
        path.addArc(range, index * sweepAngle, sweepAngle)

        textPaint.color = if (backgroundColor.isColorDark()) Color.WHITE else Color.BLACK

        val textWidth: Float = textPaint.measureText(text)
        val hOffset = (radius * Math.PI / itemList.size - textWidth / 2).toFloat()
        // TODO: 2021/6/13 Add top padding
        val vOffset = defaultBorderWidthDp.convertDpToPixel(context).toFloat() + 40
        canvas.drawTextOnPath(text, path, hOffset, vOffset, textPaint)
    }

    // TODO: 2021/6/6 Extract const
    private fun drawImage(canvas: Canvas, index: Int, sweepAngle: Float, bitmap: Bitmap) {
        val imgWidth = (radius / 2 * Math.PI) * 0.7 / itemList.size

        val angle = (index * sweepAngle + 360f / itemList.size / 2)
        val angleDegree = angle * Math.PI / 180

        val x = (radius * 0.6 * Math.cos(angleDegree)).toFloat()
        val y = (radius * 0.6 * Math.sin(angleDegree)).toFloat()

        val rect = Rect(
            -imgWidth.toInt(), -imgWidth.toInt(),
            imgWidth.toInt(), imgWidth.toInt()
        )
        canvas.withTranslationCustom(x, y) {
            rotate(90 + angle)
            canvas.drawBitmap(bitmap, null, rect, null)
        }
    }

    fun setPieViewStateListener(listener: PieViewStateListener) {
        this.listener = listener
    }

    fun rotateTo(
        index: Int,
        rotationDirection: RotationDirection = RotationDirection.Clockwise,
        durationInMilliSeconds: Long = 5000L,
        numberOfRound: Int = 15,
        timeInterpolator: TimeInterpolator = AccelerateInterpolator()
    ) {
        rotation %= 360f
        animate()
            .setInterpolator(timeInterpolator)
            .setDuration(durationInMilliSeconds / 2)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    isRunning = true
                    listener?.onRotateStart(index, durationInMilliSeconds)
                }

                override fun onAnimationEnd(animation: Animator?) {
                    rotation = 0f
                    decelerateAnimation(index, durationInMilliSeconds / 2, numberOfRound)
                }

                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            })
            .rotation(numberOfRound * 360f * rotationDirection.value)
            .start()
    }

    // Set the last round
    private fun decelerateAnimation(
        index: Int,
        durationInMilliSeconds: Long,
        numberOfRound: Int
    ) {
        val sweepAngle = 360 / itemList.size - 2
        val offset =
            Random.Default.nextInt(sweepAngle) - sweepAngle / 2 // -sweepAngle / 2 ~ sweepAngle / 2
        val targetDegree =
            numberOfRound * 360f + 270f - index.getAngleOfIndexTarget(itemList.size) - 360f / itemList.size / 2 + offset

        animate()
            .setInterpolator(DecelerateInterpolator())
            .setDuration(durationInMilliSeconds)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    isRunning = false
                    rotation %= 360f
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
    enum class RotationDirection(val value: Int) {
        Clockwise(1), Counterclockwise(-1)
    }

    interface PieViewStateListener {
        fun onRotateStart(index: Int, rotateDurationInMilliSeconds: Long)
        fun onItemSelected(item: LuckyItem)
    }

    // TODO: remove, set from WheelView
    companion object {
        private const val defaultBorderWidthDp = 15
        private const val defaultPaddingDp = 30
        private const val touchThreshold = 700
    }
}
