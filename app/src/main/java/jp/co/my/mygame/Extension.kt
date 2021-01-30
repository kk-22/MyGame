package jp.co.my.mygame

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import io.realm.RealmList

fun Int.createBitmap(width: Int, height: Int, context: Context): Bitmap {
    val baseImage = BitmapFactory.decodeResource(context.resources, this)
    return Bitmap.createScaledBitmap(baseImage, width, height, true)
}

fun <T> RealmList<T>.deleteObject(obj: T) {
    val index = indexOf(obj)
    deleteFromRealm(index)
}

fun <T> RealmList<T>.deleteAllObject(objs: List<T>) {
    objs.forEach { deleteObject(it) }
}

fun String.log() {
    Log.d("tag", this)
}

fun String.toast(context: Context) {
    log()
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}