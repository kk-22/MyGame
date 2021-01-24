package jp.co.my.mysen.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import jp.co.my.mygame.R
import jp.co.my.mygame.createBitmap
import jp.co.my.mysen.model.SEGameBalance
import jp.co.my.mysen.model.SELand
import jp.co.my.mysen.model.SEUnit

class SEFieldView(context: Context, attrs: AttributeSet) : SosotataImageView(context, attrs) {
    // 外部参照
    lateinit var listener: Listener
    private lateinit var balance: SEGameBalance

    // 情報
    private lateinit var lands: List<SELand>
    private var allUnits: MutableList<SEUnit> = mutableListOf()
    private var highlightedLands: MutableList<SELand> = mutableListOf()

    // 描画
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
        paint.color = BORDER_COLOR
        renderCanvas.drawRect(0.0f, 0.0f, width.toFloat(), height.toFloat(), paint)
        // 地形描画
        lands.forEach { drawLand(it) }
        setImage(sourceBitmap)
    }

    private fun drawLand(land: SELand) {
        renderCanvas.drawBitmap(
            SELand.Type.image(context, land.type),
            land.pointX,
            land.pointY,
            null
        )

        if (land.units.isNotEmpty()) {
            renderCanvas.drawBitmap(
                R.drawable.se_unit.createBitmap(UNIT_WIDTH, UNIT_HEIGHT, context),
                land.pointX + (LAND_WIDTH_AND_HEIGHT - UNIT_WIDTH) / 2,
                land.pointY + (LAND_WIDTH_AND_HEIGHT - UNIT_WIDTH) / 2,
                null
            )
        }
    }

    private fun drawHighlight(land: SELand, isHighlight: Boolean) {
        val width = LAND_MARGIN.toFloat()
        val paint = Paint().apply {
            color = if (isHighlight) Color.argb(255, 255, 0, 0) else BORDER_COLOR
            strokeWidth = width
            style = Paint.Style.STROKE
        }

        val x = land.pointX - width
        val y = land.pointY - width
        renderCanvas.drawRect(
            x, y,
            x + LAND_WIDTH_AND_HEIGHT.toFloat() + width * 2 - 1,
            y + LAND_WIDTH_AND_HEIGHT.toFloat() + width * 2 - 1, paint
        )
    }

    fun highlightLands(lands: List<SELand>) {
        lands.forEach { drawHighlight(it, true) }
        highlightedLands.addAll(lands)
    }

    fun clearHighlight() {
        highlightedLands.forEach { drawHighlight(it, false) }
        highlightedLands.clear()
    }

    fun getLand(x: Int, y: Int): SELand? {
        if (x < 0 || balance.fieldNumberOfX <= x || y < 0 || balance.fieldNumberOfY <= y) {
            return null
        }
        return lands[x + y * balance.fieldNumberOfX]
    }

    fun moveUnit(unit: SEUnit, toLand: SELand) {
        if (unit.currentLand == toLand) {
            // 新たに出撃したユニットを登録
            allUnits.add(unit)
        } else {
            unit.currentLand.units.remove(unit)
            drawLand(unit.currentLand)
        }
        toLand.units.add(unit)
        unit.currentLand = toLand
        drawLand(toLand)
    }

    // 入城・帰還
    fun enterUnits(units: List<SEUnit>) {
        val toLand = units.first().currentLand
        toLand.units.removeAll(units)
        allUnits.removeAll(units)
        drawLand(toLand)
    }

    fun moveAllUnit() {
        allUnits.forEach { unit ->
            unit.stackedMovingPower += unit.general.movingPower
            unit.nextLand()
                ?.takeIf { it.canEnter(unit) }
                ?.also {
                    unit.stackedMovingPower = 0
                    moveUnit(unit, it)
                }
        }
    }

    companion object {
        const val LAND_WIDTH_AND_HEIGHT: Int = 50
        const val UNIT_WIDTH: Int = 25
        const val UNIT_HEIGHT: Int = 20
        const val LAND_MARGIN: Int = 1
        private const val BORDER_COLOR: Int = Color.BLACK
    }

    interface Listener {
        fun onClickLand(land: SELand)
    }
}