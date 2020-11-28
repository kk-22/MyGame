package jp.co.my.mygame

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import jp.co.my.mygame.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_WEB_VIEW = 1234
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.boxTable.boxCells.forEach {
            it.setOnClickListener(cellClickListener)
        }
        if (binding.boxTable.loadFromPref()) {
            binding.boxTable.validateAllCell()
        }
        binding.footerBar.numberToggles.forEach { it.setOnClickListener(numberClickListener) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_WEB_VIEW) {
            data?.getStringArrayExtra("NUMBER_ARRAY")?.also { numbers ->
                binding.boxTable.clearCells()
                binding.boxTable.boxCells.forEachIndexed { index, cell ->
                    cell.setAnswer(numbers[index] ?: "")
                }
                binding.boxTable.validateAllCell()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.su_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_import -> {
                val intent = Intent(applicationContext, SUWebViewActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_WEB_VIEW)
                true
            }
            R.id.menu_clear -> {
                binding.boxTable.clearCells()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val cellClickListener = View.OnClickListener {
        val cell = it as SUBoxCell
        val oldAnswer = cell.getAnswer()
        val changedAnswers = mutableListOf(oldAnswer)
        val selectedNumber = binding.footerBar.selectedNumber()
        var newAnswer = ""
        var newNote: String? = null
        selectedNumber?.also { number ->
            when {
                oldAnswer == number -> {
                    // 前の操作時に誤って入力した数字を削除する
                    if (binding.footerBar.binding.noteToggle.isChecked) {
                        newNote = number
                    }
                }
                oldAnswer != "" && cell.status != SUStatus.ERROR -> {
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
        cell.setAnswer(newAnswer)
        newNote?.also { note -> cell.toggleNote(note) }
        // Stateの更新
        cell.highlightIfNeeded(selectedNumber, false)
        changedAnswers.forEach { answer ->
            if (answer == "") { return@forEach }
            val cells = binding.boxTable.filteredCells(answer)
            binding.boxTable.validateCells(cells)
            val countOnlyAnswer = cells.count { cell -> cell.hasAnswer() && cell.status != SUStatus.ERROR }
            // フッターの更新
            when {
                countOnlyAnswer == SUBoxTable.MAX_ROWS ->
                    // 9セル分の入力が完了した数字は無効化する
                    binding.footerBar.enableToggle(false, answer)
                countOnlyAnswer == SUBoxTable.MAX_ROWS - 1 && newAnswer == "" ->
                    binding.footerBar.enableToggle(true, answer)
            }
        }
        binding.boxTable.saveToPref()
    }

    private val numberClickListener = View.OnClickListener {
        val toggle = it as ToggleButton
        var selectingNumber: String? = null
        if (toggle.isChecked) {
            // 選択状態は1つのみにする
            binding.footerBar.deselectToggles(toggle)
            selectingNumber = toggle.textOn.toString()
        }
        binding.boxTable.highlightCell(selectingNumber)
    }
}