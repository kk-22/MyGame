package jp.co.my.mysen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet

class SEFieldView(context: Context, attrs: AttributeSet) : SosotataImageView(context, attrs) {

    private lateinit var balance: SEGameBalance
    private lateinit var lands: List<SELand>

    private lateinit var sourceBitmap: Bitmap
    private lateinit var renderCanvas: Canvas

    fun initialize(balance: SEGameBalance, lands: List<SELand>) {
        this.balance = balance

        val width = LAND_WIDTH_AND_HEIGHT * balance.fieldNumberOfX + LAND_MARGIN * (balance.fieldNumberOfX + 1)
        val height = LAND_WIDTH_AND_HEIGHT * balance.fieldNumberOfY + LAND_MARGIN * (balance.fieldNumberOfY + 1)
        sourceBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        renderCanvas = Canvas(this.sourceBitmap)
        this.lands = lands

        // 塗りつぶし
        val paint = Paint()
        paint.color = Color.BLACK
        renderCanvas.drawRect(0.0f, 0.0f, width.toFloat(), height.toFloat(), paint)
        // 地形描画
        lands.forEach { drawLand(it) }
        setImage(sourceBitmap)
    }

    private fun drawLand(x: Int, y: Int) {
        drawLand(getLand(x, y))
    }

    private fun drawLand(land: SELand) {
        renderCanvas.drawBitmap(
            SELand.Type.image(context, land.type),
            (LAND_WIDTH_AND_HEIGHT * land.x + LAND_MARGIN * (land.x + 1)).toFloat(),
            (LAND_WIDTH_AND_HEIGHT * land.y + LAND_MARGIN * (land.y + 1)).toFloat(),
            null
        )
    }

    private fun getLand(x: Int, y: Int) : SELand {
        return lands[x + y * balance.fieldNumberOfX]
    }

    companion object {
        const val LAND_WIDTH_AND_HEIGHT: Int = 50
        private const val LAND_MARGIN: Int = 1
    }
}