package jp.co.my.sudoku

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import org.json.JSONArray


class SUBoxTable(context: Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet) {

    var boxCells = emptyArray<SUBoxCell>()

    companion object {
        const val MAX_ROWS: Int = 9
        private const val BORDER_WIDTH_NORMAL: Int = 2
        private const val BORDER_WIDTH_BOLD: Int = 10
    }

    init {
        createCells()
    }

    fun filteredCells(answer: String): List<SUBoxCell> {
        return boxCells.filter { it.hasNumber(answer) }
    }

    fun validateCells(cells: List<SUBoxCell>) {
        // 縦・横・グループ毎に分別
        val groupList = Array(MAX_ROWS) { mutableListOf<SUBoxCell>() }
        val xList = Array(MAX_ROWS) { mutableListOf<SUBoxCell>() }
        val yList = Array(MAX_ROWS) { mutableListOf<SUBoxCell>() }
        cells.forEach { cell ->
            groupList[cell.group].add(cell)
            xList[cell.x].add(cell)
            yList[cell.y].add(cell)
        }
        // 検証
        listOf(groupList, xList, yList).forEach { list ->
            list.forEach inner@{ cells ->
                if (cells.count() <= 1) { return@inner } // 重複は無いためエラー無し
                val answerCount = cells.count { it.hasAnswer() }
                if (answerCount == 0) { return@inner } // メモ同士なら問題無し
                cells.forEach innerCell@{ cell ->
                    if (answerCount == 1 && cell.hasAnswer()) { return@innerCell } // 答えが1つならNoteだけエラー表示
                    cell.updateState(SUStatus.ERROR)
                }
            }
        }
    }

    fun resetError() {
        boxCells.forEach { cell ->
            if (cell.status == SUStatus.ERROR) {
                cell.updateState(SUStatus.NORMAL)
            }
        }
    }

    fun validateAllCell() {
        resetError()
        val answerList = Array(10) { mutableListOf<SUBoxCell>() }
        boxCells.forEach { cell ->
            if (cell.hasAnswer()) {
                answerList[Integer.valueOf(cell.getAnswer())].add(cell)
                return@forEach
            }
            cell.noteNumbers.forEach { answerList[Integer.valueOf(it)].add(cell) }
        }
        answerList.forEach { validateCells(it) }
    }

    fun highlightCell(highlightAnswer: String?) {
        // 縦・横・グループ毎に数字が入力済みならtrue
        val answeredEachGroup = Array(MAX_ROWS) { false }
        val answeredEachX = Array(MAX_ROWS) { false }
        val answeredEachY = Array(MAX_ROWS) { false }
        val blankCells = mutableListOf<SUBoxCell>()
        boxCells.forEach { cell ->
            if (cell.status == SUStatus.ERROR) { return@forEach }
            cell.highlightIfNeeded(highlightAnswer) // ハイライトリセット
            if (highlightAnswer == null) { return@forEach }
            if (cell.getAnswer() == highlightAnswer) {
                answeredEachGroup[cell.group] = true
                answeredEachX[cell.x] = true
                answeredEachY[cell.y] = true
            } else if (!cell.hasAnswer()) {
                blankCells.add(cell)
            }
        }
        // 数字が入力可能なセルをハイライト
        blankCells.forEach { cell ->
            if (!answeredEachGroup[cell.group] && !answeredEachX[cell.x] && !answeredEachY[cell.y]) {
                cell.updateState(SUStatus.HIGHLIGHT)
            }
        }
    }

    fun saveToPref() {
        val texts: List<String> = boxCells.map { it.getAnswer() + "," + it.noteNumbers.joinToString(separator = "") }
        val jsonArray = JSONArray(texts)
        val pref = getDefaultSharedPreferences(context)
        pref.edit().putString("SUCellTexts", jsonArray.toString()).apply()
    }

    fun loadFromPref(): Boolean {
        val pref = getDefaultSharedPreferences(context)
        val json = pref.getString("SUCellTexts", "")
        if (json == "") return false
        val jsonArray = JSONArray(json)
        for (i in 0 until jsonArray.length()) {
            val texts = jsonArray.getString(i).split(",")
            boxCells[i].setAnswer(texts[0])
            boxCells[i].resetNote(texts[1].split("").filter { it != "" })
        }
        return true
    }

    private fun createCells() {
        // 画面サイズ
        val metrics = DisplayMetrics()
        val windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(metrics)

        val constraintSet = ConstraintSet()
        constraintSet.clone(this)
        var topCell: SUBoxCell? = null // 1つ前の行の左端のセル
        for (y in 0 until MAX_ROWS) { // 行
            var leftCell: SUBoxCell? = null // 左のセル
            for (x in 0 until MAX_ROWS) { // 列
                val group = x / 3 + (y / 3 * 3)
                val cell = SUBoxCell(context, x = x, y = y, group = group)
                cell.id = 100 + y * 10 + x
                boxCells += cell
                addView(cell)

                val width = metrics.widthPixels / (MAX_ROWS + 1)
                constraintSet.constrainWidth(cell.id, width)
                constraintSet.constrainHeight(cell.id, width)
                constraintSet.setMargin(cell.id, 1, if (x % 3 == 0) BORDER_WIDTH_BOLD else BORDER_WIDTH_NORMAL)
                constraintSet.setMargin(cell.id, 2, if (x % 3 == 2) BORDER_WIDTH_BOLD else BORDER_WIDTH_NORMAL)
                leftCell?.also {
                    // 2～9列目
                    constraintSet.connect(cell.id, ConstraintSet.LEFT, it.id, ConstraintSet.RIGHT)
                    constraintSet.connect(cell.id, ConstraintSet.TOP, it.id, ConstraintSet.TOP)
                } ?: run {
                    // 1列目
                    constraintSet.connect(cell.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT )
                    constraintSet.setMargin(cell.id, 3, if (y % 3 == 0) BORDER_WIDTH_BOLD else BORDER_WIDTH_NORMAL)
                    constraintSet.setMargin(cell.id, 4, if (y % 3 == 2) BORDER_WIDTH_BOLD else BORDER_WIDTH_NORMAL)
                    topCell?.also {
                        constraintSet.connect(cell.id, ConstraintSet.TOP, it.id, ConstraintSet.BOTTOM)
                        if (y == MAX_ROWS - 1) {
                            // 最終行
                            constraintSet.connect(cell.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                        }
                    } ?: run {
                        constraintSet.connect(cell.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                    }
                }
                if (x == 8) {
                    // 最終列
                    constraintSet.connect(cell.id, ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)
                    topCell = cell
                    leftCell = null
                } else {
                    leftCell = cell
                }
            }
        }
        constraintSet.applyTo(this)
    }
}
