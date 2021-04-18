package com.fantasyfang.materialluckywheel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fantasyfang.materialluckywheel.model.LuckyItem

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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

        findViewById<MaterialLuckyWheelView>(R.id.lucky_view).setItemList(items)
    }
}