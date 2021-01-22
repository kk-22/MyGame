package jp.co.my.mysen

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import jp.co.my.mygame.R

class SEFieldView(context: Context, attrs: AttributeSet) : SosotataImageView(context, attrs) {

    private lateinit var balance: SEGameBalance

    private lateinit var mSourceBitmap: Bitmap
    private lateinit var mRenderCanvas: Canvas

    fun initialize(balance: SEGameBalance) {
        this.balance = balance

        val width = LAND_WIDTH_AND_HEIGHT * balance.fieldNumberOfX + LAND_MARGIN * (balance.fieldNumberOfX + 1)
        val height = LAND_WIDTH_AND_HEIGHT * balance.fieldNumberOfY + LAND_MARGIN * (balance.fieldNumberOfY + 1)
        mSourceBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mRenderCanvas = Canvas(this.mSourceBitmap)
        // 塗りつぶし
        val paint = Paint()
        paint.color = Color.BLACK
        mRenderCanvas.drawRect(0.0f, 0.0f, width.toFloat(), height.toFloat(), paint)
        // 地形描画
        for (y in 0 until balance.fieldNumberOfY) {
            for (x in 0 until balance.fieldNumberOfX) {
                drawLand(x, y)
            }
        }
        setImage(mSourceBitmap)
    }

    private fun drawLand(x: Int, y: Int) {
        val image = if ((x + y) % 2 == 0) {
            BitmapFactory.decodeResource(context.resources, R.drawable.se_land_grass)
        } else {
            BitmapFactory.decodeResource(context.resources, R.drawable.se_land_highway)
        }
        val scaled = Bitmap.createScaledBitmap(
            image,
            LAND_WIDTH_AND_HEIGHT,
            LAND_WIDTH_AND_HEIGHT,
            true
        )
        mRenderCanvas.drawBitmap(
            scaled,
            (LAND_WIDTH_AND_HEIGHT * x + LAND_MARGIN * (x + 1)).toFloat(),
            (LAND_WIDTH_AND_HEIGHT * y + LAND_MARGIN * (y + 1)).toFloat(),
            null
        )
    }

    companion object {
        private const val LAND_WIDTH_AND_HEIGHT: Int = 50
        private const val LAND_MARGIN: Int = 1
    }
}