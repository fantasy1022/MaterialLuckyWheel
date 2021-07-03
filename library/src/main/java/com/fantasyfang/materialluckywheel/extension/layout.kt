package com.fantasyfang.materialluckywheel.extension

import android.content.Context
import android.util.TypedValue

fun Int.convertDpToPixel(context: Context) =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(),
        context.resources.displayMetrics
    ).toInt()
