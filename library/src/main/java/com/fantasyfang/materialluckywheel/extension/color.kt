package com.fantasyfang.materialluckywheel.extension

import androidx.core.graphics.ColorUtils

fun Int.isColorDark() = ColorUtils.calculateLuminance(this) <= 0.3
