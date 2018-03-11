package com.moodii.app.helpers

import android.content.Context
import android.view.View
import android.view.MotionEvent
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.GestureDetector



/**
 * Created by kevb on 06/02/2018.
 * OnSwipeTouchListener extends OnTouchListener to detect left and right swipes on view.
 *
 * Call as follows
 *   myView.setOnTouchListener(
 *     object : OnSwipeTouchListener(this) {
 *      override fun onSwipeLeft() {
 *      Log.d("Debug", "Action was LEFT")
 *      }
 *    }
) */

open class OnSwipeTouchListener constructor(context: Context): View.OnTouchListener {
    private var gestureDetector = GestureDetector(context, GestureListener())

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    open fun onSwipeLeft() {
    }
    open fun onSwipeRight() {
    }  //for override

    private inner class GestureListener : SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            val swipeDistanceThreshold = 100
            val swipeVelocityThreshold = 100
            val distanceX = e2.x - e1.x
            val distanceY = e2.y - e1.y
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > swipeDistanceThreshold && Math.abs(velocityX) > swipeVelocityThreshold) {
                if (distanceX > 0)
                    onSwipeRight()
                else
                    onSwipeLeft()
                return true
            }
            return false
        }
    }
}
