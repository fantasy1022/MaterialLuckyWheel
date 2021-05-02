package com.fantasyfang.materialluckywheel.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

data class LuckyItem(
    val text: String,
    @DrawableRes val icon: Int,
    @ColorRes val backgroundColor: Int
)
