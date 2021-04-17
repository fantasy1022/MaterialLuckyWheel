package com.fantasyfang.materialluckywheel.model

import java.lang.Math.sqrt

data class Vector(var x: Float, var y: Float) {

    val length: Double
        get() = sqrt(x * x.toDouble() + y * y)

    fun move(x: Float, y: Float) {
        this.x += x
        this.y += y
    }

    fun set(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    infix fun scale(number: Float): Vector =
        Vector(x * number, y * number)

}
