package jp.co.my.mysen.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import jp.co.my.mygame.R
import jp.co.my.mygame.createBitmap
import jp.co.my.mysen.realm.SELandRealmObject
import jp.co.my.mysen.realm.SEPlayerRealmObject
import jp.co.my.mysen.realm.SEUnitRealmObject

class SEFieldView(context: Context, attrs: AttributeSet) : SosotataImageView(context, attrs) {
    // 外部参照
    lateinit var listener: Listener
    private lateinit var playerObject: SEPlayerRealmObject

    // 情報
    private lateinit var lands: List<SELandRealmObject>
    private var allUnits: MutableList<SEUnitRealmObject> = mutableListOf()
    private var highlightedLands: MutableList<SELandRealmObject> = mutableListOf()

    // 描画
    private lateinit var sourceBitmap: Bitmap
    private lateinit var renderCanvas: Canvas
    private var landImages: MutableMap<String, Bitmap> = mutableMapOf()
    private var unitImages: MutableMap<String, Bitmap> = mutableMapOf()

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

    fun initialize(playerObject: SEPlayerRealmObject, lands: List<SELandRealmObject>) {
        this.playerObject = playerObject

        val width = LAND_WIDTH_AND_HEIGHT * playerObject.fieldNumberOfX + LAND_MARGIN * (playerObject.fieldNumberOfX + 1)
        val height = LAND_WIDTH_AND_HEIGHT * playerObject.fieldNumberOfY + LAND_MARGIN * (playerObject.fieldNumberOfY + 1)
        sourceBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        renderCanvas = Canvas(this.sourceBitmap)
        this.lands = lands

        allUnits.clear()
        lands.forEach { allUnits.addAll(it.unitObjects) }

        // 塗りつぶし
        val paint = Paint()
        paint.color = BORDER_COLOR
        renderCanvas.drawRect(0.0f, 0.0f, width.toFloat(), height.toFloat(), paint)
        // 地形描画
        lands.forEach { drawLand(it) }
        setImage(sourceBitmap)
    }

    private fun drawLand(land: SELandRealmObject) {
        renderCanvas.drawBitmap(
            getLandImage(land),
            land.pointX,
            land.pointY,
            null
        )

        land.unitObjects.firstOrNull()?.also {
            renderCanvas.drawBitmap(
                getUnitImage(it),
                land.pointX + (LAND_WIDTH_AND_HEIGHT - UNIT_WIDTH) / 2,
                land.pointY + (LAND_WIDTH_AND_HEIGHT - UNIT_WIDTH) / 2,
                null
            )
        }
    }

    private fun drawHighlight(land: SELandRealmObject, isHighlight: Boolean) {
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

    fun highlightLands(lands: List<SELandRealmObject>) {
        lands.forEach { drawHighlight(it, true) }
        highlightedLands.addAll(lands)
    }

    fun clearHighlight() {
        highlightedLands.forEach { drawHighlight(it, false) }
        highlightedLands.clear()
    }

    fun getLand(x: Int, y: Int): SELandRealmObject? {
        if (x < 0 || playerObject.fieldNumberOfX <= x || y < 0 || playerObject.fieldNumberOfY <= y) {
            return null
        }
        return lands[x + y * playerObject.fieldNumberOfX]
    }

    fun moveUnit(unit: SEUnitRealmObject, toLand: SELandRealmObject) {
        if (unit.currentLand == toLand) {
            // 新たに出撃したユニットを登録
            allUnits.add(unit)
        } else {
            val current = unit.currentLand!!
            current.unitObjects.remove(unit)
            drawLand(current)
        }
        toLand.unitObjects.add(unit)
        unit.currentLand = toLand
        drawLand(toLand)
    }

    // 入城・帰還
    fun enterUnits(units: List<SEUnitRealmObject>) {
        units.forEach {
            val toLand = it.currentLand!!
            toLand.unitObjects.removeAll(units)
            drawLand(toLand)
        }
        allUnits.removeAll(units)
    }

    fun moveAllUnit() {
        allUnits.forEach { unit ->
            unit.stackedMovingPower += unit.general!!.speed
            unit.nextLand()
                ?.takeIf { it.canEnter(unit) }
                ?.also {
                    unit.stackedMovingPower = 0
                    moveUnit(unit, it)
                }
        }
    }

    private fun getLandImage(land: SELandRealmObject): Bitmap {
        val key = land.type.title
        landImages[key]?.also { return it }
        landImages[key] = land.type.imageId.createBitmap(
            LAND_WIDTH_AND_HEIGHT,
            LAND_WIDTH_AND_HEIGHT,
            context
        )
        return landImages[key]!!
    }

    private fun getUnitImage(unit: SEUnitRealmObject): Bitmap {
        val key = "test"
        unitImages[key]?.also { return it }
        unitImages[key] = R.drawable.se_unit.createBitmap(
            UNIT_WIDTH,
            UNIT_HEIGHT,
            context
        )
        return unitImages[key]!!
    }

    companion object {
        const val LAND_WIDTH_AND_HEIGHT: Int = 50
        const val UNIT_WIDTH: Int = 25
        const val UNIT_HEIGHT: Int = 20
        const val LAND_MARGIN: Int = 1
        private const val BORDER_COLOR: Int = Color.BLACK
    }

    interface Listener {
        fun onClickLand(land: SELandRealmObject)
    }
}