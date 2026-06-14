package edu.utap.fling

import android.view.View

class Jump(private val puck: View,
           private val border: Border
) {
    // XXX remember some X and Y values and any other state
    private var corner = 0;

    private fun placePuck() {
        // XXX Write me
        val x = when (corner) {
            0, 3 -> border.minX().toFloat()
            else -> (border.maxX()-puck.width).toFloat()
        }

        val y = when (corner) {
            0, 1 -> border.minY().toFloat()
            else -> (border.maxY()-puck.width).toFloat()
        }

        puck.x = x
        puck.y = y
    }
    fun start() {
        placePuck()
        puck.visibility = View.VISIBLE
        puck.isClickable = true
        // XXX Write me
        puck.setOnClickListener {
            corner = when(corner) {
                0 -> 1
                1 -> 2
                2 -> 3
                3 -> 0
                else -> 0
            }

            placePuck()
        }
    }
    fun finish() {
        // XXX Write me
        puck.setOnClickListener(null)
        puck.visibility = View.INVISIBLE
        puck.isClickable = false
    }
}