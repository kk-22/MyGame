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
        inputTable.cells.forEach {
            it.forEach { cell ->
                cell?.setOnClickListener(cellListener)
            }
        }
        footerBar = findViewById(R.id.footer_bar)
        footerBar.numberButtons.forEach { it.setOnClickListener(footerListener) }
    }

    private val cellListener = View.OnClickListener {
        val cell = it as SUBoxCell
        val numbers = footerBar.selectingNumbers()
        when (numbers.count()) {
            0 -> { cell.center_number_text.text = "" }
            1 -> { cell.center_number_text.text = numbers[0] }
            else -> { cell.center_number_text.text = numbers.joinToString(separator = "") }
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
                    cell.center_number_text.text == selecting[0]
                }
            }
        }
    }
}