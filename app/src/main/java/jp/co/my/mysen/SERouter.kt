package jp.co.my.mysen

class SERouter(
    private val unit: SEUnit,
    private val destinationLand: SELand,
    private val fieldView: SEFieldView
) {

    private var crossedLands: MutableMap<SELand, Route> = mutableMapOf()

    init {
        val startingRoute = Route(arrayListOf(), 0)
        search(startingRoute, unit.currentLand)
    }

    fun getBestRoute(): Route? {
        return crossedLands[destinationLand]
    }

    private fun search(currentRoute: Route, nextLand: SELand) {
        val cost = if (unit.currentLand == nextLand) 0 else nextLand.movingCost(unit)
        if (cost < 0) {
            return
        }

        val newRoute = Route(currentRoute, nextLand, cost)
        crossedLands[nextLand]?.takeIf { it.totalCost <= newRoute.totalCost }?.also {
            // より短いコストで到達済み
            return
        }

        crossedLands[nextLand] = newRoute
        if (nextLand == destinationLand) {
            // 目標地点へ到達
            return
        }
        // スマホは縦長なので上下移動のルートを優先
        val plusX = arrayListOf(0, 0, 1, -1)
        val plusY = arrayListOf(-1, 1, 0, 0)
        for (i in 0..3) {
            fieldView.getLand(nextLand.x + plusX[i], nextLand.y + plusY[i])?.also {
                search(newRoute, it)
            }
        }
    }

    // ルート探索の結果を格納
    class Route(
        val lands: List<SELand>, // 通過した地形
        val totalCost: Int // 合計の移動コスト
    ) {
        constructor(prevRoute: Route, addingLand: SELand, addingCost: Int)
                : this(prevRoute.lands.plus(addingLand), prevRoute.totalCost + addingCost)
    }
}