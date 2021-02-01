package jp.co.my.mysen.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import jp.co.my.mysen.realm.*
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request

object HttpClient {
    // 必ずシングルトンにする
    val instance = OkHttpClient()
}

class SEFieldViewModel: ViewModel() {

    fun loadObject(playerObject: SEPlayerRealmObject): List<SELandRealmObject> {
        val realm = Realm.getDefaultInstance()
        var lands: List<SELandRealmObject> = realm.where<SELandRealmObject>().findAll()
        if (0 < lands.count()) {
            Log.d("tag", lands.toString())
        } else {
            Log.d("tag", "Make new lands")
            val mockTypes = arrayOf(
                SELandRealmObject.Type.Grass,SELandRealmObject.Type.Grass,SELandRealmObject.Type.Fort,SELandRealmObject.Type.Highway,SELandRealmObject.Type.Grass,
                SELandRealmObject.Type.Grass,SELandRealmObject.Type.Mountain,SELandRealmObject.Type.Grass,SELandRealmObject.Type.Highway,SELandRealmObject.Type.Grass,
                SELandRealmObject.Type.Grass,SELandRealmObject.Type.Mountain,SELandRealmObject.Type.Grass,SELandRealmObject.Type.Highway,SELandRealmObject.Type.Grass,
                SELandRealmObject.Type.Grass,SELandRealmObject.Type.Grass,SELandRealmObject.Type.Highway,SELandRealmObject.Type.Highway,SELandRealmObject.Type.Grass,
                SELandRealmObject.Type.Grass,SELandRealmObject.Type.Grass,SELandRealmObject.Type.Fort,SELandRealmObject.Type.Grass,SELandRealmObject.Type.Grass,
            )
            realm.executeTransaction {
                val playerCountry = it.where<SECountryRealmObject>().equalTo("isPlayerCountry", true).findFirst()
                val cpuCountry = it.where<SECountryRealmObject>().equalTo("isPlayerCountry", false).findFirst()
                lands = mockTypes.mapIndexed { index, type ->
                    val land = realm.createObject<SELandRealmObject>()
                    land.setup(
                        type,
                        index % playerObject.fieldNumberOfX,
                        index / playerObject.fieldNumberOfY,
                        if (index <= mockTypes.count() / 2) cpuCountry else playerCountry
                    )
                    land
                }
            }
        }
        return lands
    }

    fun resetObject(): LiveData<Boolean> {
        return liveData(Dispatchers.IO) {
            val countryJson = httpGet("https://script.google.com/macros/s/AKfycbzVk_CvxzpIAxXhx5lroyDzayI6sV4TLoLiHYU-CgwbuHKbPTM/exec?sheet=country")
            val generalJson = httpGet("https://script.google.com/macros/s/AKfycbzVk_CvxzpIAxXhx5lroyDzayI6sV4TLoLiHYU-CgwbuHKbPTM/exec?sheet=general")
            if (countryJson == null || generalJson == null) {
                emit(false)
                return@liveData
            }
            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            // 既存データ削除
            arrayOf(SECountryRealmObject::class,
                SEGeneralRealmObject::class,
                SELandRealmObject::class,
                SERouteRealmObject::class,
                SEUnitRealmObject::class,).forEach {
                realm.delete(it.java)
            }
            // 取得データ保存
            realm.createAllFromJson(SECountryRealmObject::class.java, countryJson)
            realm.createAllFromJson(SEGeneralRealmObject::class.java, generalJson)
            // リレーションシップ登録
            val countries = realm.where<SECountryRealmObject>().sort("id").findAll()
            val generals = realm.where<SEGeneralRealmObject>().findAll()
            generals.forEach {
                it.country = countries[it.country_id.toInt()]
            }

            // 正式実装ではプレイヤーの操作によって登録するパラメータを仮設定
            countries.first()?.isPlayerCountry = true

            realm.commitTransaction()
            emit(true)
        }
    }

     private fun httpGet(url : String): String? {
        val request = Request.Builder()
            .url(url)
            .build()

        val response = HttpClient.instance.newCall(request).execute()
        return response.body?.string()
    }
}