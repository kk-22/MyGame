package jp.co.my.mysen.realm

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request

object HttpClient {
    // 必ずシングルトンにする
    val instance = OkHttpClient()
}

class SEBaseRealmViewModel: ViewModel() {

    fun createBaseRealms(): LiveData<Boolean> {
        return liveData(Dispatchers.IO) {
            val response = httpGet("https://script.google.com/macros/s/AKfycbzVk_CvxzpIAxXhx5lroyDzayI6sV4TLoLiHYU-CgwbuHKbPTM/exec?sheet=country")
            response?.also {
                val realm = Realm.getDefaultInstance()
                realm.where<SECountryBaseRealm>().findAll().deleteAllFromRealm()

                realm.executeTransaction {
                    it.createAllFromJson(SECountryBaseRealm::class.java, response)
                }
                emit(true)
            } ?: run {
                emit(false)
            }
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