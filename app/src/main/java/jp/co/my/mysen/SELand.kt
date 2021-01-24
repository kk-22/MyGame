package jp.co.my.mysen

import android.content.Context
import android.graphics.Bitmap
import jp.co.my.mygame.R
import jp.co.my.mygame.createBitmap

class SELand(val type: Type,
             val x: Int,
             val y: Int) {

    val pointX =
        (SEFieldView.LAND_WIDTH_AND_HEIGHT * x + SEFieldView.LAND_MARGIN * (x + 1)).toFloat()
    val pointY =
        (SEFieldView.LAND_WIDTH_AND_HEIGHT * y + SEFieldView.LAND_MARGIN * (y + 1)).toFloat()
    val units: MutableList<SEUnit> = mutableListOf()

    fun movingCost(unit: SEUnit): Int {
        return type.basicCost
    }

    // この地形にユニットが進入可能ならtrueを返す
    fun canEnter(unit: SEUnit): Boolean {
        if (unit.stackedMovingPower < movingCost(unit)) {
            return false // 移動力不足
        }
        return true
    }

    enum class Type(val title: String, val basicCost: Int, val imageId: Int) {
        Highway("道", 10, R.drawable.se_land_highway),
        Grass("草原", 30, R.drawable.se_land_grass),
        Mountain("山", -1, R.drawable.se_land_mountain),
        Fort("砦", 50, R.drawable.se_land_fort),
        ;

        companion object {
            private var images: MutableMap<String, Bitmap> = mutableMapOf()
            fun image(context: Context, type: Type): Bitmap {
                images[type.title]?.also { return it }
                images[type.title] = type.imageId.createBitmap(
                    SEFieldView.LAND_WIDTH_AND_HEIGHT,
                    SEFieldView.LAND_WIDTH_AND_HEIGHT,
                    context
                )
                return images[type.title]!!
            }
        }
    }
}