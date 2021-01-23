package jp.co.my.mysen

import android.content.Context
import android.graphics.Bitmap
import jp.co.my.mygame.R
import jp.co.my.mygame.createBitmap

class SELand(val type: Type,
             val x: Int,
             val y: Int) {

    val units: MutableList<SEUnit> = mutableListOf()

    enum class Type(val title: String, val imageId: Int) {
        Highway("道", R.drawable.se_land_highway),
        Grass("草原", R.drawable.se_land_grass),
        Fort("砦", R.drawable.se_land_fort),
        ;

        companion object {
            private var images: MutableMap<String, Bitmap> = mutableMapOf()
            fun image(context: Context, type: Type) : Bitmap {
                images[type.title]?.also { return it }
                images[type.title] = type.imageId.createBitmap(
                    SEFieldView.LAND_WIDTH_AND_HEIGHT,
                    SEFieldView.LAND_WIDTH_AND_HEIGHT,
                    context)
                return images[type.title]!!
            }
        }
    }
}