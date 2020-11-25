package jp.co.my.mygame

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.su_box_cell.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var inputTable: SUInputTable
    private lateinit var footerBar: SUFooterBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputTable = findViewById(R.id.input_table)
        inputTable.boxCells.forEach {
            it.setOnClickListener(cellListener)
        }
        footerBar = findViewById(R.id.footer_bar)
        footerBar.numberButtons.forEach { it.setOnClickListener(footerListener) }
    }

    private val cellListener = View.OnClickListener {
        val cell = it as SUBoxCell
        val numbers = footerBar.selectingNumbers()
        val oldAnswer = cell.answer_text.text.toString()
        val changedAnswers = mutableListOf(oldAnswer)
        var newAnswer = ""
        var newNote = ""
        when (numbers.count()) {
            0 -> { /* Reset all text */ }
            1 -> {
                newAnswer = numbers[0]
                if (oldAnswer == newAnswer) { return@OnClickListener }
                changedAnswers.add(newAnswer)
            }
            else -> {
                newNote = numbers.joinToString(separator = "")
            }
        }
        cell.answer_text.text = newAnswer
        cell.note_text.text = newNote
        if (newAnswer == "") {
            cell.updateState(SUStatus.NORMAL) // numbersが1以外ならvalidateCellsの対象外なのでここでリセット
        }
        changedAnswers.forEach { answer ->
            if (answer == "") { return@forEach }
            inputTable.validateCells(inputTable.filteredCells(answer))
        }
    }

    private val footerListener = View.OnClickListener {
        val selecting = footerBar.selectingNumbers()
        when (selecting.count()) {
            0, 2 -> {
                // ハイライト解除
                inputTable.updateAllStatus(SUStatus.NORMAL) { cell ->
                    cell.status == SUStatus.SAME
                }
            }
            1 -> {
                // 数字が同じCellをハイライト
                inputTable.updateAllStatus(SUStatus.SAME) { cell ->
                    cell.answer_text.text == selecting[0]
                }
            }
        }
    }
}