package jp.co.my.mysen.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import jp.co.my.mygame.databinding.SeViewInfoStackBinding
import jp.co.my.mysen.realm.SELandRealmObject

class SEInfoStackView(context: Context, attributeSet: AttributeSet) :
    ConstraintLayout(context, attributeSet) {

    private val binding = SeViewInfoStackBinding.inflate(LayoutInflater.from(context), this, true)

    fun updateLand(land: SELandRealmObject) {
        binding.nameText.text = land.type.name
    }
}
