package jp.co.my.mygame

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import jp.co.my.mygame.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.inputTable.boxCells.forEach {
            it.setOnClickListener(cellClickListener)
        }
        if (binding.inputTable.loadFromPref()) {
            binding.inputTable.validateAllCell()
        }
        binding.footerBar.numberToggles.forEach { it.setOnClickListener(numberClickListener) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.su_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear -> {
                binding.inputTable.clearCells()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val cellClickListener = View.OnClickListener {
        val cell = it as SUBoxCell
        val oldAnswer = cell.binding.answerText.text.toString()
        val changedAnswers = mutableListOf(oldAnswer)
        val selectedNumber = binding.footerBar.selectedNumber()
        var newAnswer = ""
        var newNote = ""
        selectedNumber?.also { number ->
            when {
                oldAnswer == number -> {
                    // 前の操作時に誤って入力した数字を削除する
                    newAnswer = ""
                }
                oldAnswer != "" -> {
                    // 誤った上書きを阻止
                    return@OnClickListener
                }
                binding.footerBar.binding.noteToggle.isChecked -> {
                    newNote = number
                }
                else -> {
                    newAnswer = number
                }
            }
            changedAnswers.add(number)
        } ?: run {
            newNote = oldAnswer // 誤って上書きした時用のバックアップ
        }

        // 数字の更新
        cell.binding.answerText.text = newAnswer
        if (newNote == "") {
            cell.resetNote(null)
        } else {
            cell.toggleNote(newNote)
        }
        // Stateの更新
        cell.highlightIfNeeded(selectedNumber, false)
        changedAnswers.forEach { answer ->
            if (answer == "") { return@forEach }
            val cells = binding.inputTable.filteredCells(answer)
            binding.inputTable.validateCells(cells)
            // フッターの更新
            when {
                cells.count() == SUInputTable.MAX_ROWS ->
                    // 9セル分の入力が完了した数字は無効化する
                    binding.footerBar.enableToggle(false, answer)
                cells.count() == SUInputTable.MAX_ROWS - 1 && oldAnswer == answer ->
                    binding.footerBar.enableToggle(true, answer)
            }
        }
        binding.inputTable.saveToPref()
    }

    private val numberClickListener = View.OnClickListener {
        val toggle = it as ToggleButton
        var selectingNumber: String? = null
        if (toggle.isChecked) {
            // 選択状態は1つのみにする
            binding.footerBar.deselectToggles(toggle)
            selectingNumber = toggle.textOn.toString()
        }
        binding.inputTable.highlightCell(selectingNumber)
    }
}