package com.adeptions.wordladder.ui

import android.view.MotionEvent
import android.view.View

class SwipeDetector(private val view: View, private val listener: OnSwipeListener): View.OnTouchListener {
    private var minimumDistance: Float = 100f
    private var downX: Float = 0f
    private var downY: Float = 0f

    init {
        view.setOnTouchListener(this)
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                val distanceX = downX - event.x
                val distanceY = downY - event.y
                val deltaX = Math.abs(distanceX)
                val deltaY = Math.abs(distanceY)
                if (deltaY > deltaX) {
                    if (deltaY > minimumDistance) {
                        listener.onSwipe(view, if (distanceY > 0) {
                            SwipeType.BOTTOM_TO_TOP
                        } else {
                            SwipeType.TOP_TO_BOTTOM
                        })
                    }
                } else {
                    if (deltaX > minimumDistance) {
                        listener.onSwipe(view, if (distanceX > 0) {
                            SwipeType.RIGHT_TO_LEFT
                        } else {
                            SwipeType.LEFT_TO_RIGHT
                        })
                    }
                }
            }
        }
        return true
    }

    fun interface OnSwipeListener {
        fun onSwipe(view: View, type: SwipeType)
    }

    enum class SwipeType {
        RIGHT_TO_LEFT, LEFT_TO_RIGHT, TOP_TO_BOTTOM, BOTTOM_TO_TOP
    }

}