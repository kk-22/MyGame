package jp.co.my.mygame

import android.os.Bundle
import android.view.View
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.su_box_cell.view.*
import kotlinx.android.synthetic.main.su_footer_bar.view.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        input_table.boxCells.forEach {
            it.setOnClickListener(cellListener)
        }
        footer_bar.numberToggles.forEach { it.setOnClickListener(footerListener) }
    }

    private val cellListener = View.OnClickListener {
        val cell = it as SUBoxCell
        val numbers = footer_bar.selectingNumbers()
        val oldAnswer = cell.answer_text.text.toString()
        val changedAnswers = mutableListOf(oldAnswer)
        var newAnswer = ""
        var newNote = ""
        if (numbers.count() == 1 && !footer_bar.note_toggle.isChecked) {
            newAnswer = numbers[0]
            if (oldAnswer == newAnswer) { return@OnClickListener }
            changedAnswers.add(newAnswer)
        } else {
            newNote = numbers.joinToString(separator = "")
        }
        cell.answer_text.text = newAnswer
        cell.note_text.text = newNote
        if (newAnswer == "") {
            cell.updateState(SUStatus.NORMAL) // numbersが1以外ならvalidateCellsの対象外なのでここでリセット
        }
        changedAnswers.forEach { answer ->
            if (answer == "") { return@forEach }
            input_table.validateCells(input_table.filteredCells(answer))
        }
    }

    private val footerListener = View.OnClickListener {
        val toggle = it as ToggleButton
        if (!footer_bar.note_toggle.isChecked && toggle.isChecked) {
            // 選択状態は1つのみにする
            footer_bar.disableToggles(toggle)
        }
        val selecting = footer_bar.selectingNumbers()
        when (selecting.count()) {
            0, 2 -> {
                // ハイライト解除
                input_table.updateAllStatus(SUStatus.NORMAL) { cell ->
                    cell.status == SUStatus.HIGHLIGHT
                }
            }
            1 -> {
                // 数字が同じCellをハイライト
                input_table.updateAllStatus(SUStatus.HIGHLIGHT) { cell ->
                    cell.answer_text.text == selecting[0]
                }
            }
        }
    }
}