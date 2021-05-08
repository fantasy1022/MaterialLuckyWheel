package com.fantasyfang.materialluckywheel.extension

import android.content.Context
import android.util.DisplayMetrics

fun Int.convertDpToPixel(context: Context) =
    this * context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT
