package jp.co.my.sudoku

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import jp.co.my.mygame.MainActivity
import jp.co.my.mygame.R
import jp.co.my.mygame.databinding.SuPlayActivityBinding

class SUPlayActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_WEB_VIEW = 1234
    }

    private lateinit var binding: SuPlayActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SuPlayActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.boxTable.boxCells.forEach {
            it.setOnClickListener(cellClickListener)
        }
        if (binding.boxTable.loadFromPref()) {
            binding.boxTable.validateAllCell()
        }
        binding.footerView.numberToggles.forEach { it.setOnClickListener(numberClickListener) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_WEB_VIEW) {
            data?.getStringArrayExtra("NUMBER_ARRAY")?.also { numbers ->
                clearCells()
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
                clearCells()
                true
            }
            R.id.menu_back -> {
                MainActivity.backToMain(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val cellClickListener = View.OnClickListener {
        val cell = it as SUBoxCell
        val oldAnswer = cell.getAnswer()
        val changedAnswers = mutableListOf(oldAnswer)
        val selectedNumber = binding.footerView.selectedNumber()
        var newAnswer = ""
        var newNote: String? = null
        selectedNumber?.also { number ->
            when {
                oldAnswer == number -> {
                    // ????????????????????????????????????????????????????????????
                    if (binding.footerView.isEnableNote()) {
                        newNote = number
                    }
                }
                oldAnswer != "" && cell.status != SUStatus.ERROR -> {
                    // ???????????????????????????
                    return@OnClickListener
                }
                binding.footerView.isEnableNote() -> {
                    newNote = number
                }
                else -> {
                    newAnswer = number
                }
            }
            changedAnswers.add(number)
        } ?: run {
            newNote = oldAnswer // ???????????????????????????????????????????????????
        }

        // ???????????????
        cell.setAnswer(newAnswer)
        newNote?.also { note -> cell.toggleNote(note) }
        // State?????????
        if (oldAnswer == "" && oldAnswer != newAnswer) {
            changedAnswers.addAll(cell.noteNumbers)
            changedAnswers.distinct()
        }
        cell.highlightIfNeeded(selectedNumber, false)
        binding.boxTable.resetError()
        changedAnswers.forEach { answer ->
            if (answer == "") { return@forEach }
            var cells = binding.boxTable.filteredCells(answer)
            if (answer == newAnswer) {
                cells = cells.filter { sameCell ->
                    // ??????????????????????????????????????????????????????
                    if (!sameCell.hasAnswer() &&
                        (sameCell.x == cell.x || sameCell.y == cell.y || sameCell.group == cell.group)) {
                        sameCell.toggleNote(newAnswer)
                        return@filter false
                    }
                    return@filter true
                }
            }
            binding.boxTable.validateCells(cells)
            val countOnlyAnswer = cells.count { cell -> cell.hasAnswer() && cell.status != SUStatus.ERROR }
            // ?????????????????????
            when {
                countOnlyAnswer == SUBoxTable.MAX_ROWS && countOnlyAnswer == cells.count() ->
                    // 9?????????????????????????????????????????????????????????
                    binding.footerView.enableToggle(false, answer)
                countOnlyAnswer == SUBoxTable.MAX_ROWS - 1 && newAnswer == "" ->
                    binding.footerView.enableToggle(true, answer)
            }
        }
        binding.boxTable.highlightCell(selectedNumber) // ?????????????????????Cell???????????????????????????
        binding.boxTable.saveToPref()
    }

    private val numberClickListener = View.OnClickListener {
        val toggle = it as ToggleButton
        var selectingNumber: String? = null
        if (toggle.isChecked) {
            // ???????????????1??????????????????
            binding.footerView.deselectToggles(toggle)
            selectingNumber = toggle.textOn.toString()
        }
        binding.boxTable.highlightCell(selectingNumber)
    }

    private fun clearCells() {
        // ?????????????????????????????????????????????preference??????????????????
        binding.boxTable.boxCells.forEach { cell ->
            cell.setAnswer("")
            cell.resetNote(null)
            cell.updateState(SUStatus.NORMAL)
        }
        binding.footerView.numberToggles.forEach {
            it.isEnabled = true
            it.isChecked = false
        }
    }
}