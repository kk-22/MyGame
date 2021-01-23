package jp.co.my.mygame

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

fun Int.createBitmap(width: Int, height: Int, context: Context): Bitmap {
    val baseImage = BitmapFactory.decodeResource(context.resources, this)
    return Bitmap.createScaledBitmap(baseImage, width, height, true)
}