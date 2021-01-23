package jp.co.my.mysen

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import jp.co.my.mygame.R
import jp.co.my.mygame.createBitmap

class SEFieldView(context: Context, attrs: AttributeSet) : SosotataImageView(context, attrs) {

    lateinit var listener: Listener
    private lateinit var balance: SEGameBalance
    private lateinit var lands: List<SELand>

    private lateinit var sourceBitmap: Bitmap
    private lateinit var renderCanvas: Canvas

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_UP && !isScrolling) {
            val values = FloatArray(9)
            this.mRenderMatrix.getValues(values)
            val widthAndHeight = LAND_WIDTH_AND_HEIGHT + LAND_MARGIN
            val x = ((e.x - values[Matrix.MTRANS_X]) / values[Matrix.MSCALE_X] / widthAndHeight)
            val y = ((e.y - values[Matrix.MTRANS_Y]) / values[Matrix.MSCALE_Y] / widthAndHeight)
            if (0 <= x && 0 <= y) { // -0.1をtoInt()すると0になるため、キャスト前に負数か確認する
                getLand(x.toInt(), y.toInt())?.also { listener.onClickLand(it) }
            }
        }
        return super.onTouchEvent(e)
    }

    fun initialize(balance: SEGameBalance, lands: List<SELand>) {
        this.balance = balance

        val width =
            LAND_WIDTH_AND_HEIGHT * balance.fieldNumberOfX + LAND_MARGIN * (balance.fieldNumberOfX + 1)
        val height =
            LAND_WIDTH_AND_HEIGHT * balance.fieldNumberOfY + LAND_MARGIN * (balance.fieldNumberOfY + 1)
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
        getLand(x, y)?.also { drawLand(it) }
    }

    private fun drawLand(land: SELand) {
        val x = (LAND_WIDTH_AND_HEIGHT * land.x + LAND_MARGIN * (land.x + 1)).toFloat()
        val y = (LAND_WIDTH_AND_HEIGHT * land.y + LAND_MARGIN * (land.y + 1)).toFloat()
        renderCanvas.drawBitmap(SELand.Type.image(context, land.type), x, y, null)

        if (land.units.isNotEmpty()) {
            renderCanvas.drawBitmap(
                R.drawable.se_unit.createBitmap(UNIT_WIDTH, UNIT_HEIGHT, context),
                x + (LAND_WIDTH_AND_HEIGHT - UNIT_WIDTH) / 2,
                y + (LAND_WIDTH_AND_HEIGHT - UNIT_HEIGHT) / 2,
                null
            )
        }
    }

    private fun getLand(x: Int, y: Int): SELand? {
        val index = x + y * balance.fieldNumberOfX
        if (index < 0 || lands.size <= index) {
            return null
        }
        return lands[index]
    }

    fun moveUnit(unit: SEUnit, toLand: SELand) {
        unit.currentLand.units.remove(unit)
        drawLand(unit.currentLand)

        toLand.units.add(unit)
        drawLand(toLand)
    }

    // 入城・帰還
    fun enterUnits(units: List<SEUnit>) {
        val toLand = units.first().currentLand
        toLand.units.removeAll(units)
        drawLand(toLand)
    }

    companion object {
        const val LAND_WIDTH_AND_HEIGHT: Int = 50
        const val UNIT_WIDTH: Int = 25
        const val UNIT_HEIGHT: Int = 20
        private const val LAND_MARGIN: Int = 1
    }

    interface Listener {
        fun onClickLand(land: SELand)
    }
}