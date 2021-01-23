package jp.co.my.mysen

class SEUnit(
    private val startingLand: SELand // 出発地点・所属拠点
) {
    var currentLand: SELand = startingLand // 現在地
    var destinationLand: SELand? = null // 目標地点
}