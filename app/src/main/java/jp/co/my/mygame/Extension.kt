package jp.co.my.mygame

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast

fun Int.createBitmap(width: Int, height: Int, context: Context): Bitmap {
    val baseImage = BitmapFactory.decodeResource(context.resources, this)
    return Bitmap.createScaledBitmap(baseImage, width, height, true)
}

fun String.log() {
    Log.d("tag", this)
}

fun String.toast(context: Context) {
    log()
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}