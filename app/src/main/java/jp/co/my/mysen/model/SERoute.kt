package jp.co.my.mysen.model

import jp.co.my.mysen.realm.SEUnitRealmObject
import jp.co.my.mysen.view.SEFieldView

class SERoute(
    val lands: List<SELand>, // 通過する地形
    private val totalCost: Int // 合計の移動コスト
) {
    constructor(prevSERoute: SERoute, addingLand: SELand, addingCost: Int)
            : this(prevSERoute.lands.plus(addingLand), prevSERoute.totalCost + addingCost)

    companion object {
        fun bestRoute(unit: SEUnitRealmObject,
                      destinationLand: SELand,
                      fieldView: SEFieldView): SERoute? {
            val firstRoute = SERoute(arrayListOf(unit.currentLand!!), 0)
            if (destinationLand == unit.currentLand) return firstRoute

            val remainingSERoutes: MutableList<SERoute> = mutableListOf(firstRoute)
            val searchedLands : MutableList<SELand> = mutableListOf(unit.currentLand!!)
            while (remainingSERoutes.isNotEmpty()) {
                val prevRoute = remainingSERoutes.first()
                val prevLand = prevRoute.lands.last()
                remainingSERoutes.remove(prevRoute)

                // スマホは縦長なので上下移動のルートを優先
                val plusX = arrayListOf(0, 0, 1, -1)
                val plusY = arrayListOf(-1, 1, 0, 0)
                for (i in 0..3) {
                    fieldView.getLand(prevLand.x + plusX[i], prevLand.y + plusY[i])?.also { nextLand ->
                        if (searchedLands.contains(nextLand)) return@also // 戻るの阻止。既に最短ルートで検索済み
                        val cost = nextLand.movingCost(unit)
                        if (cost < 0) return@also

                        val nextRoute = SERoute(prevRoute, nextLand, cost)
                        if (nextLand == destinationLand) {
                            // 目標地点へ到達
                            return nextRoute
                        }
                        remainingSERoutes.add(nextRoute)
                        searchedLands.add(nextLand)
                    }
                }
                // 最小コストのprevRouteを順次取り出せるようにソート
                remainingSERoutes.sortBy { it.totalCost }
            }
            return null
        }
    }
}