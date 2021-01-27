package jp.co.my.mysen.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class SECountryBaseRealm: RealmObject() {
    @PrimaryKey
    var no: Int? = null

    @Required
    var name: String = ""
}
