package jp.co.my.mysen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import jp.co.my.mygame.R

class SELand(val type: Type,
             val x: Int,
             val y: Int) {

    enum class Type(val title: String, val imageId: Int) {
        Highway("道", R.drawable.se_land_highway),
        Grass("草原", R.drawable.se_land_grass),
        Fort("砦", R.drawable.se_land_fort),
        ;

        fun getImage(context: Context) : Bitmap {
            return image(context, this)
        }

        companion object {
            private var images: MutableMap<String, Bitmap> = mutableMapOf()
            fun image(context: Context, type: Type) : Bitmap {
                images[type.title]?.let { return it }
                val baseImage = BitmapFactory.decodeResource(context.resources, type.imageId)
                val scaledImage = Bitmap.createScaledBitmap(
                    baseImage,
                    SEFieldView.LAND_WIDTH_AND_HEIGHT,
                    SEFieldView.LAND_WIDTH_AND_HEIGHT,
                    true
                )
                images[type.title] = scaledImage
                return scaledImage
            }
        }
    }
}