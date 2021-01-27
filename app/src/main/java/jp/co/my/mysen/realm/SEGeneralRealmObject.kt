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
    @Required
    var physical_attack: Int? = 0
    @Required
    var physical_defence: Int? = 0
    @Required
    var special_attack: Int? = 0
    @Required
    var special_defence: Int? = 0
    @Required
    var speed: Int? = 0
}