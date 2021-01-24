package jp.co.my.mysen.model

import jp.co.my.mysen.view.SEFieldView

class SERouter(
    private val unit: SEUnit,
    private val destinationLand: SELand,
    private val fieldView: SEFieldView
) {

    var bestRoute: Route?

    init {
        bestRoute = searchBestRoute()
    }

    private fun searchBestRoute(): Route? {
        val firstRoute = Route(arrayListOf(unit.currentLand), 0)
        if (destinationLand == unit.currentLand) return firstRoute

        val remainingRoutes: MutableList<Route> = mutableListOf(firstRoute)
        val searchedLands : MutableList<SELand> = mutableListOf(unit.currentLand)
        while (remainingRoutes.isNotEmpty()) {
            val prevRoute = remainingRoutes.first()
            val prevLand = prevRoute.lands.last()
            remainingRoutes.remove(prevRoute)

            // スマホは縦長なので上下移動のルートを優先
            val plusX = arrayListOf(0, 0, 1, -1)
            val plusY = arrayListOf(-1, 1, 0, 0)
            for (i in 0..3) {
                fieldView.getLand(prevLand.x + plusX[i], prevLand.y + plusY[i])?.also { nextLand ->
                    if (searchedLands.contains(nextLand)) return@also // 戻るの阻止。既に最短ルートで検索済み
                    val cost = nextLand.movingCost(unit)
                    if (cost < 0) return@also

                    val nextRoute = Route(prevRoute, nextLand, cost)
                    if (nextLand == destinationLand) {
                        // 目標地点へ到達
                        return nextRoute
                    }
                    remainingRoutes.add(nextRoute)
                    searchedLands.add(nextLand)
                }
            }
            // 最小コストのprevRouteを順次取り出せるようにソート
            remainingRoutes.sortBy { it.totalCost }
        }
        return null
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