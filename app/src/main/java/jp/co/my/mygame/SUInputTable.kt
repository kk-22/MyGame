package jp.co.my.mygame

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.android.synthetic.main.su_box_cell.view.*


class SUInputTable(context: Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet) {

    private val cells = Array(9) { arrayOfNulls<SUBoxCell>(9) } // [列][行]
    private val MAX_ROWS: Int = 9

    init {
        createCells()
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
                val cell = SUBoxCell(context, x, y)
                cell.id = 100 + y * 10 + x
                cells[y][x] = cell
                cell.center_number_text.text = cell.id.toString()
                addView(cell)

                constraintSet.setDimensionRatio(cell.id, "1:1")
                constraintSet.constrainWidth(cell.id, metrics.widthPixels / 9)
                constraintSet.constrainHeight(cell.id, 0)
                leftCell?.let {
                    // 2～9列目
                    constraintSet.connect(cell.id, ConstraintSet.LEFT, it.id, ConstraintSet.RIGHT)
                    constraintSet.connect(cell.id, ConstraintSet.TOP, it.id, ConstraintSet.TOP)
                } ?: run {
                    // 1列目
                    constraintSet.connect(cell.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT )
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
            constraintSet.createHorizontalChain(
                ConstraintSet.PARENT_ID,
                ConstraintSet.RIGHT,
                ConstraintSet.PARENT_ID,
                ConstraintSet.LEFT,
                cells[y].mapNotNull { it?.id }.toIntArray(),
                null,
                ConstraintSet.CHAIN_PACKED
            )
        }
        constraintSet.createVerticalChain(
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM,
            cells.mapNotNull { it[0]?.id }.toIntArray(),
            null,
            ConstraintSet.CHAIN_PACKED
        )
        constraintSet.applyTo(this)
    }
}
