package jp.co.my.mygame

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ToggleButton
import androidx.constraintlayout.widget.ConstraintLayout
import jp.co.my.mygame.databinding.SuFooterBarBinding

class SUFooterView(context: Context, attributeSet: AttributeSet) : ConstraintLayout(
    context,
    attributeSet
) {

    private val binding = SuFooterBarBinding.inflate(LayoutInflater.from(context), this, true)
    val numberToggles: List<ToggleButton>

    init {
        val numberIds = arrayOf(
            R.id.number1_toggle, R.id.number2_toggle, R.id.number3_toggle, R.id.number4_toggle,
            R.id.number5_toggle, R.id.number6_toggle, R.id.number7_toggle, R.id.number8_toggle, R.id.number9_toggle
        )
        numberToggles = numberIds.map { findViewById<ToggleButton>(it) }

        binding.leftNoteToggle.setOnCheckedChangeListener { _, value -> binding.rightNoteToggle.isChecked = value }
        binding.rightNoteToggle.setOnCheckedChangeListener { _, value -> binding.leftNoteToggle.isChecked = value }
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

    fun enableToggle(newEnable: Boolean, number: String) {
        numberToggles.forEach { toggle ->
            if (toggle.textOn != number) { return@forEach }
            if (toggle.isEnabled == newEnable) { return }
            toggle.isEnabled = newEnable
            toggle.isChecked = false
        }
    }

    fun isEnableNote(): Boolean {
        return binding.leftNoteToggle.isChecked
    }
}