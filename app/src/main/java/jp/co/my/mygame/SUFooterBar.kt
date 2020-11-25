package jp.co.my.mygame

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ToggleButton
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.su_footer_bar.view.*


class SUFooterBar(context: Context, attributeSet: AttributeSet) : ConstraintLayout(
    context,
    attributeSet
) {

    val numberToggles: List<ToggleButton>
    init {
        View.inflate(context, R.layout.su_footer_bar, this)

        val numberIds = arrayOf(
            R.id.number1_toggle, R.id.number2_toggle, R.id.number3_toggle, R.id.number4_toggle,
            R.id.number5_toggle, R.id.number6_toggle, R.id.number7_toggle, R.id.number8_toggle, R.id.number9_toggle
        )
        numberToggles = numberIds.map { findViewById<ToggleButton>(it) }

        note_toggle.setOnClickListener {
            if (note_toggle.isChecked || selectingNumbers().count() <= 1) { return@setOnClickListener }
            disableToggles()  // 2つ以上選択中にオフにしたら全数字をオフ
        }
    }

    fun selectingNumbers(): List<String> {
        return numberToggles.mapNotNull { if (it.isChecked) it.textOn as String else null }
    }

    fun disableToggles(ignoreToggle: ToggleButton? = null) {
        numberToggles.forEach { toggle ->
            if (toggle == ignoreToggle) { return@forEach }
            toggle.isChecked = false
        }
    }
}