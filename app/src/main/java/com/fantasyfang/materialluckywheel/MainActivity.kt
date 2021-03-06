package com.fantasyfang.materialluckywheel

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fantasyfang.materialluckywheel.extension.convertDpToPixel
import com.fantasyfang.materialluckywheel.model.LuckyItem
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private val items = listOf(
        LuckyItem(
            "Telegram",
            R.drawable.telegram,
            R.color.red
        ),
        LuckyItem(
            "WhatsApp",
            R.drawable.whatsapp,
            R.color.orange
        ),
        LuckyItem(
            "Skype",
            R.drawable.skype,
            R.color.yellow
        ),
        LuckyItem(
            "Messenger",
            R.drawable.messenger,
            R.color.light_blue
        ),
        LuckyItem(
            "LINE",
            R.drawable.line,
            R.color.blue
        ),
        LuckyItem(
            "Slack",
            R.drawable.slack,
            R.color.purple
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val luckyWheelView = findViewById<MaterialLuckyWheelView>(R.id.lucky_view)
        luckyWheelView.setItemList(items)
        luckyWheelView.isTouchEnabled = true
//        setCustomParameter(luckyWheelView)

        luckyWheelView.setOnLuckyWheelViewStateListener { item ->
            Toast.makeText(this@MainActivity, "Select ${item.text} !", Toast.LENGTH_LONG)
                .show()
        }

        luckyWheelView.setOnLuckyWheelItemGoListener {
            luckyWheelView.startLuckyWheelWithTargetIndex(
                getRandomIndex()
            )
        }
    }

    private fun getRandomIndex(): Int {
        return Random.Default.nextInt(items.size)
    }

    private fun setCustomParameter(luckyWheelView: MaterialLuckyWheelView) {
        with(luckyWheelView) {
            setOuterRingWidth(30.convertDpToPixel(this@MainActivity))
            setOuterRingWidth(30.convertDpToPixel(this@MainActivity))
            setOuterRingColor(Color.WHITE)
            setPieTextSize(14.convertDpToPixel(this@MainActivity))
            setPieEdgeWidth(5.convertDpToPixel(this@MainActivity))
            setPieEdgeColor(Color.WHITE)
            setCenterRadius(60.convertDpToPixel(this@MainActivity))
            setCenterText("Go")
            setCenterTextSizeInSp(14f)
            setCenterTextColor(Color.WHITE)
            setCenterBackgroundColor(Color.WHITE)
        }
    }
}
