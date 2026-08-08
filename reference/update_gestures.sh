#!/bin/bash
for f in app/src/main/java/com/example/service/TriggerHandleView.kt app/src/main/java/com/example/service/ReaderHandleView.kt; do
  sed -i '/private fun setupListeners() {/,/})/{
    /private fun setupListeners() {/!d
    /private fun setupListeners() {/a\
        val gestureDetector = android.view.GestureDetector(context, object : android.view.GestureDetector.SimpleOnGestureListener() {\
            override fun onDown(e: android.view.MotionEvent): Boolean {\
                return true\
            }\
            override fun onSingleTapConfirmed(e: android.view.MotionEvent): Boolean {\
                handleAction("tap")\
                return true\
            }\
            override fun onDoubleTap(e: android.view.MotionEvent): Boolean {\
                handleAction("double_tap")\
                return true\
            }\
            override fun onLongPress(e: android.view.MotionEvent) {\
                handleAction("long_press")\
            }\
            override fun onFling(e1: android.view.MotionEvent?, e2: android.view.MotionEvent, velocityX: Float, velocityY: Float): Boolean {\
                if (e1 != null) {\
                    val dx = e2.x - e1.x\
                    val dy = e2.y - e1.y\
                    if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {\
                        if (dx > 50) handleAction("swipe_right")\
                        else if (dx < -50) handleAction("swipe_left")\
                    } else {\
                        if (dy > 50) handleAction("swipe_down")\
                        else if (dy < -50) handleAction("swipe_up")\
                    }\
                    return true\
                }\
                return false\
            }\
        })\
        handleView?.setOnTouchListener { _, event ->\
            gestureDetector.onTouchEvent(event)\
        }\
    }
  }' $f
done
