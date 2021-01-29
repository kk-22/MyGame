package jp.co.my.mysen.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class SEGeneralRealmObject: RealmObject() {
    @PrimaryKey
    var id: Int? = null

    @Required
    var country_id: String = ""
    var country: SECountryRealmObject? = null

    @Required
    var name: String = ""
    var physical_attack: Int = 0
    var physical_defence: Int = 0
    var special_attack: Int = 0
    var special_defence: Int = 0
    var speed: Int = 0
}