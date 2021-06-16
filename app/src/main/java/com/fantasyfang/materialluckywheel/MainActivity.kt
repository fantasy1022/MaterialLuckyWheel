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

        val luckyWheelLayout = findViewById<MaterialLuckyWheelLayout>(R.id.lucky_view)
        luckyWheelLayout.setItemList(items)
        luckyWheelLayout.isTouchEnabled = true

        luckyWheelLayout.luckyWheelLayoutStateListener =
            object : // TODO: 2021/6/13 Change to lambda
                MaterialLuckyWheelLayout.LuckyWheelLayoutStateListener {
                override fun onItemSelected(item: LuckyItem) {
                    Toast.makeText(this@MainActivity, "Select ${item.text} !", Toast.LENGTH_LONG)
                        .show()
                }

                override fun onRotateStart() {
                }
            }

        luckyWheelLayout.luckyWheelItemGoListener = object : // TODO: 2021/6/13 Change to lambda
            MaterialLuckyWheelLayout.LuckyWheelItemGoListener {
            override fun onClick(view: View) {
                luckyWheelLayout.startLuckyWheelWithTargetIndex(getRandomIndex())
            }
        }
    }

    private fun getRandomIndex(): Int {
        return Random.Default.nextInt(items.size)
    }
}
