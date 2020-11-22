package jp.co.my.mygame

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ToggleButton
import androidx.constraintlayout.widget.ConstraintLayout

class SUFooterBar(context : Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet) {

    private val numberButtons: List<ToggleButton>
    init {
        View.inflate(context, R.layout.su_footer_bar, this)

        val numberIds = arrayOf(R.id.button1, R.id.button2, R.id.button3, R.id.button4,
            R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9)
        numberButtons = numberIds.map { findViewById<ToggleButton>(it) }

        findViewById<Button>(R.id.clear_button).setOnClickListener {
            numberButtons.forEach { toggleButton -> toggleButton.isChecked = false }
        }
    }
}