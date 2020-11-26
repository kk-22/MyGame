package jp.co.my.mygame

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import jp.co.my.mygame.databinding.SuBoxCellBinding

enum class SUStatus {
    NORMAL, HIGHLIGHT, ERROR
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

    val binding = SuBoxCellBinding.inflate(LayoutInflater.from(context), this)
    val noteNumbers = mutableListOf<String>()
    var status = SUStatus.NORMAL

    init {
        updateState()
    }

    fun updateState(next: SUStatus? = null) {
        next?.also {
            if (status == it) { return }
            status = it
        }
        when (status) {
            SUStatus.NORMAL -> setBackgroundColor(Color.WHITE)
            SUStatus.HIGHLIGHT -> setBackgroundColor(Color.YELLOW)
            SUStatus.ERROR -> setBackgroundColor(Color.RED)
        }
    }

    fun highlightIfNeeded(highlightAnswer: String?, skipIfError: Boolean = true) {
        if (status == SUStatus.ERROR && skipIfError) return
        if (highlightAnswer != null
            && (binding.answerText.text == highlightAnswer || noteNumbers.contains(highlightAnswer))) {
            updateState(SUStatus.HIGHLIGHT)
        } else {
            updateState(SUStatus.NORMAL)
        }
    }

    fun toggleNote(number: String) {
        if (noteNumbers.contains(number)) {
            noteNumbers.remove(number)
        } else {
            noteNumbers.add(number)
            noteNumbers.sort()
        }
        updateNoteText()
    }

    fun resetNote(newNumbers: List<String>?) {
        noteNumbers.clear()
        newNumbers?.also {
            noteNumbers.addAll(it)
        }
        updateNoteText()
    }

    private fun updateNoteText() {
        binding.noteText.text = noteNumbers.joinToString(separator = "")
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