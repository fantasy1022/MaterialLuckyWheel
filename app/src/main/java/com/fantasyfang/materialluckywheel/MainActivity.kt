package com.fantasyfang.materialluckywheel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fantasyfang.materialluckywheel.model.LuckyItem
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private val items = listOf(
        LuckyItem(
            "1000",
            R.drawable.chrome_icon,
            R.color.teal_200
        ),
        LuckyItem(
            "9000",
            R.drawable.amazon_icon,
            R.color.teal_700
        ),
        LuckyItem(
            "2000",
            R.drawable.dropbox_icon,
            R.color.purple_700
        ),
        LuckyItem(
            "100",
            R.drawable.disney_icon,
            R.color.purple_200
        ),
        LuckyItem(
            "500",
            R.drawable.disney_icon,
            R.color.purple_200
        ),
        LuckyItem(
            "600",
            R.drawable.disney_icon,
            R.color.purple_200
        )

    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val luckyWheelLayout = findViewById<MaterialLuckyWheelLayout>(R.id.lucky_view)
        luckyWheelLayout.setItemList(items)
        luckyWheelLayout.isTouchEnabled = true
        // TODO: 2021/6/12 Set rotation listener and rotation target index 
//        luckyWheelView.setMaterialLuckyWheelViewListener(object : //TODO: change to lambda
//            MaterialLuckyWheelView.MaterialLuckyWheelViewListener {
//            override fun onItemSelected(item: LuckyItem) {
//                Toast.makeText(this@MainActivity, "Select ${item.text} !", Toast.LENGTH_LONG).show()
//            }
//        })

//        findViewById<Button>(R.id.rotate_btn).setOnClickListener {
//            luckyWheelLayout.rotateTo(
//                getRandomIndex().apply {
//                    Log.d("Fan", "rotate index: $this")
//                },
//                MaterialLuckyWheelView.RotationDirection.Clockwise,
//                5000
//            )
//        }
    }

    private fun getRandomIndex(): Int {
        return Random.Default.nextInt(items.size)
    }
}
