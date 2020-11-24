package jp.co.my.mygame

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout

class SUBoxCell
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

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