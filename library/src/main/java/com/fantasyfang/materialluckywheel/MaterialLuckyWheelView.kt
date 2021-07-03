package com.fantasyfang.materialluckywheel

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import com.fantasyfang.library.R
import com.fantasyfang.materialluckywheel.extension.convertDpToPixel
import com.fantasyfang.materialluckywheel.extension.getAngleOfIndexTarget
import com.fantasyfang.materialluckywheel.model.LuckyItem
import com.google.android.material.button.MaterialButton
import kotlin.random.Random

class MaterialLuckyWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val TAG = MaterialLuckyWheelView::class.java.simpleName

    companion object {
        private const val defaultOuterRingColor = Color.WHITE
        private const val defaultOuterRingWidthDp = 15
        private const val defaultPieTextSizeSp = 14
        private const val defaultPieEdgeWidthDp = 5
        private const val defaultCenterRadiusDp = 60
        private const val defaultCenterText = "Go"
        private const val defaultCenterTextSize = 16
        private const val defaultPieEdgeColor = Color.WHITE
        private const val defaultCenterTextColor = Color.BLACK
        private const val defaultCenterBackgroundColor = Color.WHITE
    }

    // Parameter
    private var mlwOuterRingColor: Int = 0
    private var mlwOuterRingWidth: Int = 0
    private var mlwPieTextSize: Int = 0
    private var mlwPieEdgeWidth: Int = 0
    private var mlwPieEdgeColor: Int = 0

    private var mlwCenterRadius: Int = 0
    private var mlwCenterText: String = ""
    private var mlwCenterTextSize: Int = 0
    private var mlwCenterTextColor: Int = 0
    private var mlwCenterBackgroundColor: Int = 0

    // UI component
    private var pieView: PieView
    private var rotateBtn: MaterialButton

    // Cursor config
    private var cursorView: ImageView
    private var animCursor: ObjectAnimator

    private var luckyWheelItemGoListener: LuckyWheelItemGoListener? = null

    init {
        attrs?.apply {
            val typedArray: TypedArray =
                context.obtainStyledAttributes(attrs, R.styleable.MaterialLuckyWheelView)
            with(typedArray) {
                mlwOuterRingColor = getColor(
                    R.styleable.MaterialLuckyWheelView_mlwOuterRingColor,
                    defaultOuterRingColor
                )
                mlwOuterRingWidth = getDimensionPixelSize(
                    R.styleable.MaterialLuckyWheelView_mlwOuterRingWidth,
                    defaultOuterRingWidthDp.convertDpToPixel(context)
                )
                mlwPieTextSize = getDimensionPixelSize(
                    R.styleable.MaterialLuckyWheelView_mlwPieTextSize,
                    defaultPieTextSizeSp.convertDpToPixel(context)
                )
                mlwPieEdgeWidth = getDimensionPixelSize(
                    R.styleable.MaterialLuckyWheelView_mlwPieEdgeWidth,
                    defaultPieEdgeWidthDp.convertDpToPixel(context)
                )
                mlwPieEdgeColor = getColor(
                    R.styleable.MaterialLuckyWheelView_mlwPieEdgeColor,
                    defaultPieEdgeColor
                )
                mlwCenterRadius = getDimensionPixelSize(
                    R.styleable.MaterialLuckyWheelView_mlwCenterRadius,
                    defaultCenterRadiusDp.convertDpToPixel(context)
                )
                mlwCenterText = getString(
                    R.styleable.MaterialLuckyWheelView_mlwCenterText,
                ) ?: defaultCenterText
                mlwCenterTextSize = getInt(
                    R.styleable.MaterialLuckyWheelView_mlwCenterTextSize,
                    defaultCenterTextSize
                )
                mlwCenterTextColor = getColor(
                    R.styleable.MaterialLuckyWheelView_mlwCenterTextColor,
                    defaultCenterTextColor
                )
                mlwCenterBackgroundColor = getColor(
                    R.styleable.MaterialLuckyWheelView_mlwCenterBackgroundColor,
                    defaultCenterBackgroundColor
                )
            }
            typedArray.recycle()
        }

        val constraintLayout = inflate(context, R.layout.lucky_wheel_layout, this)

        pieView = constraintLayout.findViewById(R.id.wheel_view)
        with(pieView) {
            setOuterRingWidth(mlwOuterRingWidth)
            setOuterRingColor(mlwOuterRingColor)
            setPieTextSize(mlwPieTextSize)
            setPieEdgeWidth(mlwPieEdgeWidth)
            setPieEdgeColor(mlwPieEdgeColor)
        }

        cursorView = constraintLayout.findViewById(R.id.cursorView)

        rotateBtn = constraintLayout.findViewById(R.id.press_btn1)
        with(rotateBtn) {
            setOnClickListener { view ->
                luckyWheelItemGoListener?.let {
                    it.onClick(view)
                } ?: run {
                    defaultRotate()
                }
            }

            layoutParams.width = mlwCenterRadius
            layoutParams.height = mlwCenterRadius
            text = mlwCenterText
            textSize = mlwCenterTextSize.toFloat()
            setTextColor(mlwCenterTextColor)
            setBackgroundColor(mlwCenterBackgroundColor)
        }

        animCursor = ObjectAnimator.ofFloat(cursorView, "rotation", 0f, -30f, 0f).apply {
            duration = 200
            interpolator = AccelerateInterpolator()
            repeatCount = 0
            repeatMode = ValueAnimator.RESTART
        }

        cursorView.pivotX = 0f
        cursorView.pivotY = cursorView.width / 2.toFloat()
    }

    // 上一次的總角度
    var preAngle = 0f

    private lateinit var itemList: List<LuckyItem>
    var isTouchEnabled: Boolean = false
        set(value) {
            field = value
            pieView.isTouchEnabled = value
        }

    fun interface LuckyWheelViewStateListener {
        fun onItemSelected(item: LuckyItem)
    }

    fun interface LuckyWheelItemGoListener {
        fun onClick(view: View)
    }

    private fun defaultRotate() {
        startLuckyWheelWithTargetIndex(getRandomIndex())
    }

    fun setOnLuckyWheelViewStateListener(luckyWheelViewStateListener: LuckyWheelViewStateListener) {
        pieView.setPieViewStateListener(object : // TODO: 2021/6/13 Change to lambda
                PieView.PieViewStateListener {
                override fun onItemSelected(item: LuckyItem) {
                    luckyWheelViewStateListener.onItemSelected(item)
                }

                override fun onRotateStart(index: Int, rotateDurationInMilliSeconds: Long) {
                    startCursorAnimation(index, rotateDurationInMilliSeconds)
                }
            })
    }

    fun setOnLuckyWheelItemGoListener(luckyWheelItemGoListener: LuckyWheelItemGoListener) {
        this.luckyWheelItemGoListener = luckyWheelItemGoListener
    }

    fun setOuterRingWidth(outerRingWidth: Int) {
        pieView.setOuterRingWidth(outerRingWidth)
    }

    fun setOuterRingColor(@ColorInt outerRingColor: Int) {
        pieView.setOuterRingColor(outerRingColor)
    }

    fun setPieTextSize(pieTextSize: Int) {
        pieView.setPieTextSize(pieTextSize)
    }

    fun setPieEdgeWidth(pieEdgeWidth: Int) {
        pieView.setPieEdgeWidth(pieEdgeWidth)
    }

    fun setPieEdgeColor(@ColorInt pieEdgeColor: Int) {
        pieView.setPieEdgeColor(pieEdgeColor)
    }

    fun setCenterRadius(centerRadius: Int) {
        with(rotateBtn) {
            layoutParams.width = centerRadius
            layoutParams.height = centerRadius
        }
    }

    fun setCenterText(text: String) {
        rotateBtn.text = text
    }

    fun setCenterTextSizeInSp(textSizeInSp: Float) {
        rotateBtn.textSize = textSizeInSp
    }

    fun setCenterTextColor(@ColorInt textColor: Int) {
        rotateBtn.setTextColor(textColor)
    }

    fun setCenterBackgroundColor(@ColorInt backgroundColor: Int) {
        rotateBtn.setBackgroundColor(backgroundColor)
    }

    fun startLuckyWheelWithTargetIndex(
        index: Int,
        durationInMilliSeconds: Long = 5000L,
        numberOfRound: Int = 15
    ) {
        if (index < 0) throw IllegalArgumentException()
        val targetIndex = index % itemList.size
        pieView.rotateTo(
            targetIndex,
            durationInMilliSeconds = durationInMilliSeconds,
            numberOfRound = numberOfRound
        )
    }

    private fun startCursorAnimation(targetIndex: Int, durationInMilliSeconds: Long) {
        val targetAngle: Float = 360f - targetIndex.getAngleOfIndexTarget(itemList.size)
        // TODO: 2021/6/14 Check target angle use
        ValueAnimator.ofFloat(0f, targetAngle).apply {
            duration = durationInMilliSeconds
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                Log.d("Fan", "update:${animation.animatedValue}")
                val currentAngle: Float =
                    animation.animatedValue as Float + 360f / itemList.size / 2
                // 這次轉的角度
                val angleOfThisTurn: Float = currentAngle - preAngle
                // 是否執行動畫
                val isStartAnimation = (angleOfThisTurn / (360f / itemList.size))

                if (isStartAnimation > 0f && !animCursor.isRunning) {
                    Log.d("Fan", "start() $preAngle")
                    preAngle += isStartAnimation * (360f / itemList.size)
                    animCursor.start()
                }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    Log.d("Fan", "onAnimationEnd:$animation")
                    preAngle = 0f
                }
            })
            start()
        }
    }

    fun setItemList(itemList: List<LuckyItem>) {
        this.itemList = itemList
        pieView.setItemList(itemList)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        for (i in 0 until childCount) {
            if (isMaterialLuckyWheelView(getChildAt(i))) {
                return super.dispatchTouchEvent(ev)
            }
        }
        return false
    }

    private fun isMaterialLuckyWheelView(view: View): Boolean {
        if (view is ViewGroup) {
            for (i in 0 until childCount) {
                if (isMaterialLuckyWheelView((view).getChildAt(i))) {
                    return true
                }
            }
        }
        return view is PieView
    }

    private fun getRandomIndex(): Int = Random.Default.nextInt(itemList.size)
}
