package jp.co.my.mysen.realm

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.kotlin.createObject
import jp.co.my.mysen.view.SEFieldView

open class SERouteRealmObject: RealmObject() {

    var lands: RealmList<SELandRealmObject> = RealmList() // 通過する地形
    private var totalCost: Int = 0 // 合計の移動コスト

    companion object {
        // ルートの途中計算で使うクラス
        class TempRoute(
            val lands: List<SELandRealmObject>, // 通過する地形
            val totalCost: Int // 合計の移動コスト
        ) {
            constructor(prevRoute: TempRoute, addingLand: SELandRealmObject, addingCost: Int)
                    : this(prevRoute.lands.plus(addingLand), prevRoute.totalCost + addingCost)

            fun castToObject(): SERouteRealmObject {
                val obj = Realm.getDefaultInstance().createObject<SERouteRealmObject>()
                obj.lands.addAll(lands)
                obj.totalCost = totalCost
                return obj
            }
        }

        fun bestRoute(unit: SEUnitRealmObject,
                      destinationLand: SELandRealmObject,
                      fieldView: SEFieldView): SERouteRealmObject? {
            val firstRoute = TempRoute(arrayListOf(unit.currentLand!!), 0)
            if (destinationLand == unit.currentLand) return firstRoute.castToObject()

            val remainingSERoutes: MutableList<TempRoute> = mutableListOf(firstRoute)
            val searchedLands : MutableList<SELandRealmObject> = mutableListOf(unit.currentLand!!)
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

                        val nextRoute = TempRoute(prevRoute, nextLand, cost)
                        if (nextLand == destinationLand) {
                            // 目標地点へ到達
                            return nextRoute.castToObject()
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