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
        var newAnswer = ""
        var newNote = ""
        binding.footerBar.selectedNumber()?.also { number ->
            when {
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
            cell.updateState(SUStatus.HIGHLIGHT) // 数値を変更＝数字選択中なので入力したセルをハイライトする
        } ?: run {
            newNote = oldAnswer // 誤って上書きした時用のバックアップ
            cell.updateState(SUStatus.NORMAL) // Answerを入力しない場合validateCellsの対象外なのでここでリセット
        }

        // 数字の更新
        cell.binding.answerText.text = newAnswer
        if (newNote == "") {
            cell.resetNote(null)
        } else {
            cell.toggleNote(newNote)
        }
        // Stateの更新
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