package jp.co.my.mygame

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout

@SuppressLint("ViewConstructor")
class SUBoxCell(context: Context, x: Int, y: Int) : ConstraintLayout(context) {

    init {
        LayoutInflater.from(context).inflate(R.layout.su_box_cell, this, true)
        updateState()
    }

    private fun updateState() {
        setBackgroundColor(Color.WHITE)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> setBackgroundColor(Color.LTGRAY)
            MotionEvent.ACTION_UP -> updateState()
        }
        return super.onTouchEvent(event)
    }
}