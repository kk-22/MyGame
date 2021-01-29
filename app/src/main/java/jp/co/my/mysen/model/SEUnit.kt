package jp.co.my.mysen.model

class SEUnit(
    val general: SEGeneral,
    private val startingLand: SELand // 出発地点・所属拠点
) {
    var currentLand: SELand = startingLand // 現在地
    var destinationLand: SELand? = null // 目標地点
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