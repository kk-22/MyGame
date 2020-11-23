package jp.co.my.mygame

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

class SUBoxCell(context: Context) : ConstraintLayout(context) {

    init {
        View.inflate(context, R.layout.su_box_cell, this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // 正方形にする
        setMeasuredDimension(measuredWidth, measuredWidth)
    }
}