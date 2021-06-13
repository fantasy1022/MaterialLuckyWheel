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

    var luckyWheelItemSelectedListener: MaterialLuckyWheelView.LuckyWheelItemSelectedListener? = null
        set(value) {
            value?.let {
                materialLuckyWheelView.setLuckyWheelItemSelectedListener(it)
            }
        }

    var luckyWheelItemGoListener: LuckyWheelItemGoListener? = null
        set(value) {
            field = value
        }

    interface LuckyWheelItemGoListener {
        fun onClick(view: View)
    }

//    fun setLuckyWheelItemSelectedListener(listener: MaterialLuckyWheelView.LuckyWheelItemSelectedListener) {
//        materialLuckyWheelView.setLuckyWheelItemSelectedListener(listener)
//    }

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
    }

    private fun defaultRotate() {
        startLuckyWheelWithTargetIndex(getRandomIndex())
    }

    fun startLuckyWheelWithTargetIndex(index: Int) {
        materialLuckyWheelView.rotateTo(index)
        startCursorAnimation(index)
    }

    private fun startCursorAnimation(targetIndex: Int) {
        val targetAngle: Float = 360f - targetIndex.getAngleOfIndexTarget(itemList.size)

//        animCursor.start()
        ValueAnimator.ofFloat(0f, targetAngle).apply {
            duration = 5000L // TODO: unify with wheel view
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
                    Log.d("Fan", "start()")
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
                if (isMaterialLuckyWheelView((view as ViewGroup).getChildAt(i))) {
                    return true
                }
            }
        }
        return view is MaterialLuckyWheelView
    }

    private fun getRandomIndex(): Int = Random.Default.nextInt(itemList.size)
}
