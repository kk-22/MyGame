package jp.co.my.mygame

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout

enum class SUStatus {
    NORMAL, SAME, ERROR
}

class SUBoxCell
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
    val x: Int = 0,
    val y: Int = 0,
    val group: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    var status = SUStatus.NORMAL

    init {
        LayoutInflater.from(context).inflate(R.layout.su_box_cell, this, true)
        updateState()
    }

    fun updateState(next: SUStatus? = null) {
        next?.let { status = it }
        when (status) {
            SUStatus.NORMAL -> setBackgroundColor(Color.WHITE)
            SUStatus.SAME -> setBackgroundColor(Color.YELLOW)
            SUStatus.ERROR -> setBackgroundColor(Color.RED)
        }
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