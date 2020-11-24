package jp.co.my.mygame

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet


class SUInputTable(context: Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet) {

    val cells = Array(9) { arrayOfNulls<SUBoxCell>(9) } // [列][行]

    init {
        createCells()
    }

    fun updateAllStatus(status: SUStatus, action: (SUBoxCell) -> Boolean) {
        cells.forEach { it ->
            it.forEach { cell ->
                if (action(cell!!)) {
                    cell.updateState(status)
                }
            }
        }
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
                val cell = SUBoxCell(context)
                cell.id = 100 + y * 10 + x
                cells[y][x] = cell
                addView(cell)

                val width = metrics.widthPixels / (MAX_ROWS + 1)
                constraintSet.constrainWidth(cell.id, width)
                constraintSet.constrainHeight(cell.id, width)
                constraintSet.setMargin(cell.id, 1, if (x % 3 == 0) BORDER_WIDTH_BOLD else BORDER_WIDTH_NORMAL)
                constraintSet.setMargin(cell.id, 2, if (x % 3 == 2) BORDER_WIDTH_BOLD else BORDER_WIDTH_NORMAL)
                leftCell?.let {
                    // 2～9列目
                    constraintSet.connect(cell.id, ConstraintSet.LEFT, it.id, ConstraintSet.RIGHT)
                    constraintSet.connect(cell.id, ConstraintSet.TOP, it.id, ConstraintSet.TOP)
                } ?: run {
                    // 1列目
                    constraintSet.connect(cell.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT )
                    constraintSet.setMargin(cell.id, 3, if (y % 3 == 0) BORDER_WIDTH_BOLD else BORDER_WIDTH_NORMAL)
                    constraintSet.setMargin(cell.id, 4, if (y % 3 == 2) BORDER_WIDTH_BOLD else BORDER_WIDTH_NORMAL)
                    topCell?.let {
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

    companion object {
        private const val MAX_ROWS: Int = 9
        private const val BORDER_WIDTH_NORMAL: Int = 2
        private const val BORDER_WIDTH_BOLD: Int = 10
    }
}
