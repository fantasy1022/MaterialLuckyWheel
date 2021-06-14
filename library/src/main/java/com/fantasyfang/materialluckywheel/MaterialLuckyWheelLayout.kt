package com.fantasyfang.materialluckywheel

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.fantasyfang.library.R
import com.fantasyfang.materialluckywheel.extension.getAngleOfIndexTarget
import com.fantasyfang.materialluckywheel.model.LuckyItem
import kotlin.random.Random

class MaterialLuckyWheelLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val TAG = MaterialLuckyWheelLayout::class.java.simpleName
    private var materialLuckyWheelView: MaterialLuckyWheelView
    private var rotateBtn: Button

    // Cursor config
    private var cursorView: ImageView
    private var animCursor: ObjectAnimator

    //    private var animCursorSlowDown: ValueAnimator
    // 上一次的總角度
    var preAngle = 0f

    private lateinit var itemList: List<LuckyItem>
    var isTouchEnabled: Boolean = false
        set(value) {
            field = value
            materialLuckyWheelView.isTouchEnabled = value
        }

    // TODO: 2021/6/14 do two layer listener
    var luckyWheelLayoutStateListener: LuckyWheelLayoutStateListener? = null
        set(value) {
            materialLuckyWheelView.setLuckyWheelStateListener(object : // TODO: 2021/6/13 Change to lambda
                    MaterialLuckyWheelView.LuckyWheelStateListener {
                    override fun onItemSelected(item: LuckyItem) {
                        value?.onItemSelected(item)
                    }

                    override fun onRotateStart() {
                        value?.onRotateStart()
                        // TODO: 2021/6/14 get parameter
                        startCursorAnimation(2, 5000)
                    }
                })
        }

    var luckyWheelItemGoListener: LuckyWheelItemGoListener? = null // LuckyWheelGoClickListener
        set(value) {
            field = value
        }

    interface LuckyWheelItemGoListener {
        fun onClick(view: View)
    }

    interface LuckyWheelLayoutStateListener {
        fun onRotateStart()
        fun onItemSelected(item: LuckyItem)
    }

    init {
        val constraintLayout = inflate(context, R.layout.lucky_wheel_layout, this)

        materialLuckyWheelView = constraintLayout.findViewById(R.id.wheel_view)
        cursorView = constraintLayout.findViewById(R.id.cursorView)

        rotateBtn = constraintLayout.findViewById(R.id.press_btn1)
        rotateBtn.setOnClickListener { view ->
            luckyWheelItemGoListener?.let {
                it.onClick(view)
            } ?: run {
                defaultRotate()
            }
        }

        animCursor = ObjectAnimator.ofFloat(cursorView, "rotation", 0f, -30f, 0f).apply {
            duration = 200
            interpolator = AccelerateInterpolator()
            repeatCount = 0
            repeatMode = ValueAnimator.RESTART
        }

        cursorView.pivotX = 0f
        cursorView.pivotY = cursorView.width / 2.toFloat()

//        luckyWheelStateListener =
// //            object : // TODO: 2021/6/13 Change to lambda
// //                MaterialLuckyWheelView.LuckyWheelStateListener {
// //                override fun onItemSelected(item: LuckyItem) {
// //
// //                }
// //
// //                override fun onRotateStart() {
// //                    startCursorAnimation(2, 5000)
// //                }
// //            }
    }

    private fun defaultRotate() {
        startLuckyWheelWithTargetIndex(getRandomIndex())
    }

    fun startLuckyWheelWithTargetIndex(
        index: Int,
        durationInMilliSeconds: Long = 5000L,
        numberOfRound: Int = 15
    ) {
        if (index < 0) throw IllegalArgumentException()
        val targetIndex = index % itemList.size
        materialLuckyWheelView.rotateTo(
            targetIndex,
            durationInMilliSeconds = durationInMilliSeconds,
            numberOfRound = numberOfRound
        )
//        startCursorAnimation(targetIndex, durationInMilliSeconds)
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

        //        animCursor.start()
//        if ( !animCursorSlowDown.isStarted() ) {
//            animCursorSlowDown.start();
//        }
//        animate()
//            .setInterpolator(DecelerateInterpolator())
//            .setDuration(5000L)
//            .setUpdateListener {  }
//            .rotation(targetAngle)
//            .start()
    }

    fun setItemList(itemList: List<LuckyItem>) {
        this.itemList = itemList
        materialLuckyWheelView.setItemList(itemList)
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
        return view is MaterialLuckyWheelView
    }

    private fun getRandomIndex(): Int = Random.Default.nextInt(itemList.size)
}
