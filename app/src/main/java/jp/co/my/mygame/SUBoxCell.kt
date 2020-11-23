package jp.co.my.mygame

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

class SUBoxCell(context: Context) : ConstraintLayout(context) {

    init {
        View.inflate(context, R.layout.su_box_cell, this)
    }
}