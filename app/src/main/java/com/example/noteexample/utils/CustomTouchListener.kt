package com.example.noteexample.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlin.math.abs


open class CustomTouchListener(ctx: Context?) : OnTouchListener {

    companion object {
        private const val DIFF = 10
//        private const val SWIPE_VELOCITY_THRESHOLD = 1
        private const val DISTANCE = 10
    }

    private val gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(ctx, GestureListener())
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            var result = false
            try {
                e1?.let { event1 ->
                    e2?.let { event2 ->
                        val diffY = event2.y - event1.y
                        val diffX = event2.x - event1.x
                        if (abs(diffX) > abs(diffY)) {
                            if (
                                abs(diffX) > DIFF &&
                                abs(distanceX) > DISTANCE
                            ) {
                                if (diffX > 0) {
                                    onSwipeRight()
                                } else {
                                    onSwipeLeft()
                                }
                                result = true
                            }
                        } else if (
                            abs(diffY) > DIFF
                            && abs(distanceY) > DISTANCE
                        ) {
                            if (diffY > 0) {
                                onSwipeBottom()
                            } else {
                                onSwipeTop()
                            }
                            result = true
                        }
                    }
                }

            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return result
        }

//        override fun onFling(
//            e1: MotionEvent,
//            e2: MotionEvent,
//            velocityX: Float,
//            velocityY: Float
//        ): Boolean {
//            var result = false
//            try {
//                val diffY = e2.y - e1.y
//                val diffX = e2.x - e1.x
//                if (abs(diffX) > abs(diffY)) {
//                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
//                        if (diffX > 0) {
//                            onSwipeRight()
//                        } else {
//                            onSwipeLeft()
//                        }
//                        result = true
//                    }
//                } else if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
//                    if (diffY > 0) {
//                        onSwipeBottom()
//                    } else {
//                        onSwipeTop()
//                    }
//                    result = true
//                }
//            } catch (exception: Exception) {
//                exception.printStackTrace()
//            }
//            return result
//        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            onDoubleTap()
            return true
        }
    }

    open fun onSwipeRight() {}
    open fun onSwipeLeft() {}
    open fun onSwipeTop() {}
    open fun onSwipeBottom() {}
    open fun onDoubleTap() {}
}
