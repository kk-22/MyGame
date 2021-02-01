package jp.co.my.mysen.realm

import io.realm.RealmList
import io.realm.RealmObject
import jp.co.my.mygame.R
import jp.co.my.mysen.view.SEFieldView

open class SELandRealmObject: RealmObject() {

    private var strType: String = ""
    var type: Type
        get() = Type.values().first { it.name == strType }
        set(value) {
            strType = value.name
        }
    var x: Int = 0
    var y: Int = 0
    var pointX = 0.0f
    var pointY = 0.0f
    var unitObjects: RealmList<SEUnitRealmObject> = RealmList()
    var governingCountry: SECountryRealmObject? = null // 拠点を支配する国。拠点以外は常にnull

    fun setup(type: Type, x: Int, y: Int, country: SECountryRealmObject? = null) {
        this.type = type
        this.x = x
        this.y = y
        pointX = (SEFieldView.LAND_WIDTH_AND_HEIGHT * x + SEFieldView.LAND_MARGIN * (x + 1)).toFloat()
        pointY = (SEFieldView.LAND_WIDTH_AND_HEIGHT * y + SEFieldView.LAND_MARGIN * (y + 1)).toFloat()
        if (type.isBase()) {
            if (country == null) {
                throw IllegalArgumentException("拠点のsetupメソッドはcountry引数が必須")
            }
            governingCountry = country
        }
    }

    fun movingCost(unit: SEUnitRealmObject): Int {
        return type.basicCost
    }

    // この地形にユニットが進入可能ならtrueを返す
    fun canEnter(unit: SEUnitRealmObject): Boolean {
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

        fun isBase(): Boolean {
            return this == Fort
        }
    }
}