package jp.co.my.mysen.realm

import io.realm.RealmObject
import io.realm.annotations.Ignore

open class SEPlayerRealmObject: RealmObject() {
    var currentDay = 0 // 進行フェーズの現在日

    // FieldView用
    @Ignore
    val fieldNumberOfX = 5
    @Ignore
    val fieldNumberOfY = 5
    // ViewModel用
    @Ignore
    val interfaceMaxDay = 20
}