package jp.co.my.mygame

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

@SuppressLint("ViewConstructor")
class SUBoxCell(context: Context, x: Int, y: Int) : ConstraintLayout(context) {

    init {
        View.inflate(context, R.layout.su_box_cell, this)
    }
}