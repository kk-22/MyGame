package jp.co.my.mygame

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ToggleButton
import androidx.constraintlayout.widget.ConstraintLayout
import jp.co.my.mygame.databinding.SuFooterBarBinding

class SUFooterBar(context: Context, attributeSet: AttributeSet) : ConstraintLayout(
    context,
    attributeSet
) {

    val binding = SuFooterBarBinding.inflate(LayoutInflater.from(context), this, true)
    val numberToggles: List<ToggleButton>

    init {
        val numberIds = arrayOf(
            R.id.number1_toggle, R.id.number2_toggle, R.id.number3_toggle, R.id.number4_toggle,
            R.id.number5_toggle, R.id.number6_toggle, R.id.number7_toggle, R.id.number8_toggle, R.id.number9_toggle
        )
        numberToggles = numberIds.map { findViewById<ToggleButton>(it) }
    }

    fun selectedNumber(): String? {
        return numberToggles.firstOrNull { it.isChecked }?.textOn?.toString() ?: run {
            null
        }
    }

    fun deselectToggles(ignoreToggle: ToggleButton? = null) {
        numberToggles.forEach { toggle ->
            if (toggle == ignoreToggle) { return@forEach }
            toggle.isChecked = false
        }
    }

    fun enableToggle(isEnable: Boolean, number: String) {
        numberToggles.forEach { toggle ->
            if (toggle.textOn != number) { return@forEach }
            toggle.isEnabled = isEnable
            toggle.isChecked = false
        }
    }
}