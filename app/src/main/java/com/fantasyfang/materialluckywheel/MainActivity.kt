package com.fantasyfang.materialluckywheel

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fantasyfang.materialluckywheel.model.LuckyItem
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private val items = listOf(
        LuckyItem(
            "1000",
            R.drawable.chrome_icon,
            R.color.red
        ),
        LuckyItem(
            "9000",
            R.drawable.amazon_icon,
            R.color.orange
        ),
        LuckyItem(
            "2000",
            R.drawable.dropbox_icon,
            R.color.yellow
        ),
        LuckyItem(
            "100",
            R.drawable.disney_icon,
            R.color.light_blue
        ),
        LuckyItem(
            "500",
            R.drawable.disney_icon,
            R.color.blue
        ),
        LuckyItem(
            "600",
            R.drawable.disney_icon,
            R.color.purple
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val luckyWheelView = findViewById<MaterialLuckyWheelView>(R.id.lucky_view)
        luckyWheelView.setItemList(items)
        luckyWheelView.isTouchEnabled = true
//        with(luckyWheelView) {
//            setOuterRingWidth(30.convertDpToPixel(this@MainActivity))
//            setOuterRingWidth(30.convertDpToPixel(this@MainActivity))
//            setOuterRingColor(Color.WHITE)
//            setPieTextSize(14.convertDpToPixel(this@MainActivity))
//            setPieEdgeWidth(5.convertDpToPixel(this@MainActivity))
//            setPieEdgeColor(Color.WHITE)
//            setCenterRadius(60.convertDpToPixel(this@MainActivity))
//            setCenterText("Go")
//            setCenterTextSizeInSp(14f)
//            setCenterTextColor(Color.WHITE)
//            setCenterBackgroundColor(Color.WHITE)
//        }

        luckyWheelView.luckyWheelViewStateListener =
            object : // TODO: 2021/6/13 Change to lambda
                MaterialLuckyWheelView.LuckyWheelViewStateListener {
                override fun onItemSelected(item: LuckyItem) {
                    Toast.makeText(this@MainActivity, "Select ${item.text} !", Toast.LENGTH_LONG)
                        .show()
                }

                override fun onRotateStart() {
                }
            }

        luckyWheelView.luckyWheelItemGoListener = object : // TODO: 2021/6/13 Change to lambda
            MaterialLuckyWheelView.LuckyWheelItemGoListener {
            override fun onClick(view: View) {
                luckyWheelView.startLuckyWheelWithTargetIndex(getRandomIndex())
            }
        }
    }

    private fun getRandomIndex(): Int {
        return Random.Default.nextInt(items.size)
    }
}
