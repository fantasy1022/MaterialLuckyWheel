package com.fantasyfang.materialluckywheel

import android.R.attr.data
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fantasyfang.materialluckywheel.model.LuckyItem
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    val items = listOf(
        LuckyItem(
            "1000",
            R.drawable.chrome_icon,
            R.color.teal_200
        ),
        LuckyItem(
            "9000",
            R.drawable.amazon_icon,
            R.color.teal_700
        ), LuckyItem(
            "2000",
            R.drawable.dropbox_icon,
            R.color.purple_700
        ), LuckyItem(
            "100",
            R.drawable.disney_icon,
            R.color.purple_200
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val luckyWheelView = findViewById<MaterialLuckyWheelView>(R.id.lucky_view)
        luckyWheelView.setItemList(items)
        luckyWheelView.setMaterialLuckyWheelViewListener(object :
            MaterialLuckyWheelView.MaterialLuckyWheelViewListener {
            override fun onItemSelected(item: LuckyItem) {
                Toast.makeText(this@MainActivity, "Select ${item.text} !", Toast.LENGTH_LONG).show()
            }
        })

        findViewById<Button>(R.id.rotate_btn).setOnClickListener {
            luckyWheelView.rotateTo(
                getRandomIndex().apply {
                    Log.d("Fan", "rotate index: $this")
                },
                MaterialLuckyWheelView.RotationDirection.Clockwise,
                5000
            )
        }
    }

    private fun getRandomIndex(): Int {
        return Random.Default.nextInt(items.size)
    }
}