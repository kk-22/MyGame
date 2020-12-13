package jp.co.my.mygame

import android.annotation.SuppressLint
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent


class SUFooterNumberToggle(
    context: Context,
    attrs: AttributeSet
) : androidx.appcompat.widget.AppCompatToggleButton(context, attrs) {

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled && event.action == MotionEvent.ACTION_DOWN) {
            // 無効化中を振動で通知
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val vibrationEffect = VibrationEffect.createOneShot(100, 128)
            vibrator.vibrate(vibrationEffect)
        }
        return super.onTouchEvent(event)
    }
}