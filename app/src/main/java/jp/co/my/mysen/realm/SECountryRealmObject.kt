package jp.co.my.mysen.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class SECountryRealmObject: RealmObject() {
    @PrimaryKey
    var id: Int? = null

    @Required
    var name: String = ""

    var isPlayerCountry: Boolean = false // プレイヤーが操作する国ならtrue
}
