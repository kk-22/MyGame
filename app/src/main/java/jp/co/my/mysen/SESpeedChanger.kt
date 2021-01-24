package jp.co.my.mysen

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.LinearLayout
import jp.co.my.mygame.databinding.SeSpeedChangerBinding

class SESpeedChanger @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = SeSpeedChangerBinding.inflate(LayoutInflater.from(context), this, true)
    private val buttons = arrayListOf<Button>(
        binding.speed1Button, binding.speed2Button, binding.speed3Button, binding.speed4Button)
    private lateinit var selectedButton: Button
    var listener: Listener? = null

    init {
        selectButton(buttons[2])
        val listener = OnClickListener { clickedView ->
            if (selectedButton == clickedView) return@OnClickListener // オフにはできない
            selectedButton.setBackgroundColor(Color.WHITE)
            selectButton(clickedView as Button)
        }
        buttons.forEach {
            it.setOnClickListener(listener)
        }
    }

    private fun selectButton(button: Button) {
        selectedButton = button
        selectedButton.setBackgroundColor(Color.YELLOW)
        listener?.onChangeSpeed()
    }

    fun speedRatio(): Long {
        return when (buttons.indexOf(selectedButton)) {
            0 -> 1
            1 -> 2
            2 -> 4
            else -> 8
        }
    }

    interface Listener {
        fun onChangeSpeed()
    }
}