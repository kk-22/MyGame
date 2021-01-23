package jp.co.my.mysen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import jp.co.my.mygame.R
import jp.co.my.mygame.createBitmap

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
        val x = (LAND_WIDTH_AND_HEIGHT * land.x + LAND_MARGIN * (land.x + 1)).toFloat()
        val y = (LAND_WIDTH_AND_HEIGHT * land.y + LAND_MARGIN * (land.y + 1)).toFloat()
        renderCanvas.drawBitmap(SELand.Type.image(context, land.type), x, y, null)

        if (!land.units.isEmpty()) {
            renderCanvas.drawBitmap(
                R.drawable.se_unit.createBitmap(UNIT_WIDTH, UNIT_HEIGHT, context),
                x + (LAND_WIDTH_AND_HEIGHT - UNIT_WIDTH) / 2,
                y + (LAND_WIDTH_AND_HEIGHT - UNIT_HEIGHT) / 2,
                null
            )
        }
    }

    private fun getLand(x: Int, y: Int) : SELand {
        return lands[x + y * balance.fieldNumberOfX]
    }

    fun moveUnit(unit: SEUnit, toLand: SELand) {
        unit.currentLand?.also {
            it.units.remove(unit)
            drawLand(it)
        }
        toLand.units.add(unit)
        drawLand(toLand)
    }

    companion object {
        const val LAND_WIDTH_AND_HEIGHT: Int = 50
        const val UNIT_WIDTH: Int = 25
        const val UNIT_HEIGHT: Int = 20
        private const val LAND_MARGIN: Int = 1
    }
}