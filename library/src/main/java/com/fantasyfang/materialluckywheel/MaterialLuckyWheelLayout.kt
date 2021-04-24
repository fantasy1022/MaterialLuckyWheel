package com.fantasyfang.materialluckywheel

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import com.fantasyfang.library.R
import com.fantasyfang.materialluckywheel.model.LuckyItem
import kotlin.random.Random


class MaterialLuckyWheelLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var materialLuckyWheelView: MaterialLuckyWheelView
    private var rotateBtn: Button
    private lateinit var itemList: List<LuckyItem>
    var isTouchEnabled = false

    init {
        Log.d("Fan","MaterialLuckyWheelLayout init")
        val inflater = LayoutInflater.from(getContext())
        val constraintLayout = inflater.inflate(R.layout.lucky_wheel_layout, this, false)
        materialLuckyWheelView = constraintLayout.findViewById(R.id.wheel_view)
        rotateBtn = constraintLayout.findViewById(R.id.press_btn1)
        rotateBtn.setOnClickListener {
            materialLuckyWheelView.rotateTo(getRandomIndex())
        }

        addView(constraintLayout)
    }

    fun setItemList(itemList: List<LuckyItem>) {
        this.itemList = itemList
        materialLuckyWheelView.setItemList(itemList)
    }

    private fun getRandomIndex(): Int = Random.Default.nextInt(itemList.size)

}