package jp.co.my.mygame

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.su_box_cell.view.*


class SUInputTable(context: Context, attributeSet: AttributeSet) : RecyclerView(context, attributeSet) {

    init {
        val list = Array<String>(81) {"$it"}
        adapter = SUCustomAdapter(list)
        layoutManager = GridLayoutManager(context, 9, GridLayoutManager.VERTICAL, false)
        setHasFixedSize(true)

        addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL))
    }
}

private class SUCustomAdapter(private val customList: Array<String>) :
    RecyclerView.Adapter<SUCustomAdapter.SUCustomViewHolder>() {

    class SUCustomViewHolder(val view: SUBoxCell) : RecyclerView.ViewHolder(view) {
    }

    override fun getItemCount(): Int {
        return customList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SUCustomViewHolder {
        val view = SUBoxCell(parent.context)
        return SUCustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: SUCustomViewHolder, position: Int) {
        holder.view.center_number_text.text = customList[position]
    }
}