package jp.co.my.mysen.realm

import io.realm.RealmObject
import io.realm.annotations.Ignore
import jp.co.my.mysen.model.SELand
import jp.co.my.mysen.model.SERoute

open class SEUnitRealmObject: RealmObject() {

    var general: SEGeneralRealmObject? = null
    @Ignore
    var startingLand: SELand? = null // 出発地点・所属拠点
    @Ignore
    var currentLand: SELand? = null // 現在地
    @Ignore
    var destinationLand: SELand? = null // 目標地点
    @Ignore
    var route: SERoute? = null // 目標地点へのルート

    var stackedMovingPower = 0 // Landに留まることで蓄積した移動力

    // 次の移動先を返す
    fun nextLand(): SELand? {
        route?.also {
            val nextIndex = it.lands.indexOf(currentLand) + 1
            if (it.lands.size <= nextIndex) {
                return null
            }
            return it.lands[nextIndex]
        }
        return null
    }

    fun remainingRouteLands(): List<SELand>? {
        route?.also {
            val currentIndex = it.lands.indexOf(currentLand)
            return it.lands.subList(currentIndex, it.lands.size)
        }
        return null
    }
}