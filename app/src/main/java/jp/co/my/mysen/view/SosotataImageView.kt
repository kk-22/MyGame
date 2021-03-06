package jp.co.my.mysen.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.view.GestureDetectorCompat
import kotlinx.coroutines.*
import kotlin.math.abs

/**
 * 画像を拡大縮小、縦横斜めの慣性スロール対応カスタムイメージビュー
 */
open class SosotataImageView
/**
 * コンストラクタ
 */(context: Context, attrs: AttributeSet) : View(context, attrs),
    GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener {
    /** 描画用ビットマップ（表示する画像をsetImageで設定する）*/
    private lateinit var mRenderBitmap: Bitmap

    /** 拡大縮小、スクロール演算用マトリクス */
    protected val mRenderMatrix: Matrix = Matrix()

    /** ジェスチャーディテクター（タッチイベントからフリック、長押し、スクロール等のイベントに変換） */
    private lateinit var mGestureDetector: GestureDetectorCompat

    /** スケールジェスチャーディテクター（タッチイベントから拡大縮小のイベントに変換） */
    private lateinit var mScaleGestureDetector: ScaleGestureDetector

    /** 縮小時の限界値（拡大は無制限）*/
    private var mScaleLimit: Float = 0f

    /** スクロール時の限界値 */
    private val mTranslateLimit: RectF = RectF()

    /** フリック操作時の慣性スクロールを制御するタスクのインスタンス */
    private val mFlingTask: FlingTask = FlingTask()

    protected var isScrolling = false

    /**
     * フリック操作時の慣性スクロールを制御するタスク
     */
    inner class FlingTask {
        /** 慣性スクロール制御コルーチン */
        private lateinit var flingCoroutine: FlingCoroutine

        /**
         * フリック時の慣性スクロールを実行する
         * @param velocityX 横方向の加速度（フリックの強さとして使用する）
         * @param velocityY 縦方向の加速度（フリックの強さとして使用する）
         * @param restart 実行中に再スタートする
         * */
        fun fling(velocityX: Float, velocityY: Float, restart: Boolean) {
            if (::flingCoroutine.isInitialized) {
                if (!restart && flingCoroutine.isFling) {
                    return
                }
                flingCoroutine.cleanUp()
            }
            flingCoroutine = FlingCoroutine()
            flingCoroutine.fling(velocityX, velocityY)
        }

        /**
         * 慣性スクロール制御コルーチン
         */
        inner class FlingCoroutine {
            /** スコープ */
            private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

            var isFling: Boolean = false

            /**
             * フリック時の慣性スクロールを実行する
             * @param velocityX 横方向の加速度（フリックの強さとして使用する）
             * @param velocityY 縦方向の加速度（フリックの強さとして使用する）
             * */
            @Suppress("BlockingMethodInNonBlockingContext")
            fun fling(velocityX: Float, velocityY: Float) {
                isFling = true

                scope.launch {
                    val mtr = FloatArray(9)
                    var vx = velocityX / 60     // 初速度を少し減らす（感覚での調整）
                    var vy = velocityY / 60     // 初速度を少し減らす（感覚での調整）
                    var isOverLLimit = false    // 左座標の限界値を超えた
                    var isOverRLimit = false    // 右座標の限界値を超えた
                    var isOverTLimit = false    // 上座標の限界値を超えた
                    var isOverBLimit = false    // 座標の限界値を超えた
                    var isBeforeTurnBackX = false   // 跳ね返り前フラグ
                    var isBeforeTurnBackY = false   // 跳ね返り前フラグ
                    val stopX = 0.05f            // 移動距離がこの値より小さくなったら止める
                    val stopY = 0.05f            // 移動距離がこの値より小さくなったら止める

                    //
                    // 慣性スクロールループ
                    //
                    while (isActive) {
                        translate(vx, vy)
                        mRenderMatrix.getValues(mtr)

                        //
                        // 上下左右の限界値超えチェック
                        //
                        if (!isOverRLimit) {
                            if (mTranslateLimit.left < mtr[Matrix.MTRANS_X]) {
                                if (!isOverLLimit && !isBeforeTurnBackX) {
                                    // 左座標の限界値を超えた
                                    isOverLLimit = true
                                    isBeforeTurnBackX = true
                                }
                            }
                        }
                        if (!isOverLLimit) {
                            if (mtr[Matrix.MTRANS_X] < mTranslateLimit.right) {
                                if (!isOverLLimit && !isOverRLimit && !isBeforeTurnBackX) {
                                    // 右座標の限界値を超えた
                                    isOverRLimit = true
                                    isBeforeTurnBackX = true
                                }
                            }
                        }
                        if (!isOverBLimit) {
                            if (mTranslateLimit.top < mtr[Matrix.MTRANS_Y]) {
                                if (!isOverTLimit && !isBeforeTurnBackY) {
                                    // 上座標の限界値を超えた
                                    isOverTLimit = true
                                    isBeforeTurnBackY = true
                                }
                            }
                        }
                        if (!isOverTLimit) {
                            if (mtr[Matrix.MTRANS_Y] < mTranslateLimit.bottom) {
                                if (!isOverTLimit && !isOverBLimit && !isBeforeTurnBackY) {
                                    // 下座標の限界値を超えた
                                    isOverBLimit = true
                                    isBeforeTurnBackY = true
                                }
                            }
                        }

                        //
                        // 移動距離を減らしていく
                        // ・移動限界値以内：x0.8f
                        // ・移動限界値以上（跳ね返り前）：x0.4f
                        // ・移動限界値以上（跳ね返り後）：x0.9f
                        //
                        vx *= if (isOverLLimit || isOverRLimit) {
                            if (isBeforeTurnBackX) {
                                0.4f
                            } else {
                                0.9f
                            }
                        } else {
                            0.8f
                        }
                        vy *= if (isOverTLimit || isOverBLimit) {
                            if (isBeforeTurnBackY) {
                                0.4f
                            } else {
                                0.9f
                            }
                        } else {
                            0.8f
                        }

                        //
                        // 慣性スクロール停止判定
                        // 上下左右の限界値を超えていたら跳ね返りを表現するために移動距離を再設定する
                        //
                        if (abs(vx) < stopX) {
                            if ((isOverLLimit || isOverRLimit) && isBeforeTurnBackX) {
                                // 跳ね返りのため移動距離を再設定
                                vx = if (mTranslateLimit.right > 0) {
                                    mtr[Matrix.MTRANS_X] - mTranslateLimit.left
                                } else {
                                    if (isOverLLimit) {
                                        mtr[Matrix.MTRANS_X] - mTranslateLimit.left
                                    } else {
                                        mtr[Matrix.MTRANS_X] - mTranslateLimit.left - mTranslateLimit.right
                                    }
                                }
                                vx *= -0.1f
                                isBeforeTurnBackX = false
                            } else {
                                // 左右座標の限界を超えずに終了
                                vx = 0f
                            }
                        }
                        if (abs(vy) < stopY) {
                            if ((isOverTLimit || isOverBLimit) && isBeforeTurnBackY) {
                                // 跳ね返りのため移動距離を再設定
                                vy = if (mTranslateLimit.bottom > 0) {
                                    mtr[Matrix.MTRANS_Y] - mTranslateLimit.top
                                } else {
                                    if (isOverTLimit) {
                                        mtr[Matrix.MTRANS_Y] - mTranslateLimit.top
                                    } else {
                                        mtr[Matrix.MTRANS_Y] - mTranslateLimit.top - mTranslateLimit.bottom
                                    }
                                }
                                vy *= -0.1f
                                isBeforeTurnBackY = false
                            } else {
                                // 左右
                                //上下座標の限界を超えずに終了
                                vy = 0f
                            }
                        }
                        if (vx == 0f && vy == 0f) {
                            // 縦横両方とも停止の閾値内に入った時点で終了
                            break
                        }
                        Thread.sleep(16) // 短くした方が滑らか
                    }
                    isFling = false
                }
            }

            fun cleanUp() {
                scope.cancel()
            }
        }
    }

    /**
     * 描画用ビットマップを設定する
     */
    fun setImage(bmp: Bitmap) {
        mRenderBitmap = bmp
        updateScaleLimit()
        updateTranslateLimit()
        renderBitmap()
        mFlingTask.fling(0F, 0F, true)
    }

    /**
     * 描画を更新する
     */
    private fun renderBitmap() {
        invalidate()
    }

    /**
     * [android.view.View.onSizeChanged]
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (w > 0 && h > 0) {
            if (!::mGestureDetector.isInitialized) {
                mGestureDetector = GestureDetectorCompat(context, this)
            }
            if (!::mScaleGestureDetector.isInitialized) {
                mScaleGestureDetector = ScaleGestureDetector(context, this)
            }
            updateScaleLimit()
            updateTranslateLimit()
            renderBitmap()
            mFlingTask.fling(0F, 0F, true)
        }
    }

    /**
     * [android.view.View.onDraw]
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (::mRenderBitmap.isInitialized) {
            canvas.drawBitmap(mRenderBitmap, mRenderMatrix, null)
        }
    }

    /**
     * [android.view.View.onTouchEvent]
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        mGestureDetector.onTouchEvent(e)
        mScaleGestureDetector.onTouchEvent(e)

        if (e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_CANCEL) {
            // はみ出したままになるケースの回避策
            // ・ACTION_UP：スクロールや拡大縮小の完了時
            // ・ACTION_CANCEL：２本指で操作中に３本指を追加するとACTION_CANCEL発生後にタッチイベントが止まる
            mFlingTask.fling(0F,0F, false)
            isScrolling = false
        }

        return true
    }

    /**
     * [android.view.GestureDetector.OnGestureListener.onShowPress]
     */
    override fun onShowPress(e: MotionEvent?) {
    }

    /**
     * [android.view.GestureDetector.OnGestureListener.onSingleTapUp]
     */
    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return true
    }

    /**
     * [android.view.GestureDetector.OnGestureListener.onDown]
     */
    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    /**
     * [android.view.GestureDetector.OnGestureListener.onFling]
     */
    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        mFlingTask.fling(velocityX, velocityY, true)
        return true
    }

    /**
     * [android.view.GestureDetector.OnGestureListener.onScroll]
     */
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        isScrolling = true
        var dx: Float = -distanceX
        var dy: Float = -distanceY

        // 限界を超えたときに移動距離を縮めて(x0.3)引っ張っている感覚を表現する
        val mtr = FloatArray(9)
        mRenderMatrix.getValues(mtr)
        if (mTranslateLimit.left < mtr[Matrix.MTRANS_X]) {
            dx *= 0.3F
        } else if (mtr[Matrix.MTRANS_X] < mTranslateLimit.right) {
            dx *= 0.3F
        }

        if (mTranslateLimit.top < mtr[Matrix.MTRANS_Y]) {
            dy *= 0.3F
        } else if (mtr[Matrix.MTRANS_Y] < mTranslateLimit.bottom) {
            dy *= 0.3F
        }

        translate(dx, dy)
        return true
    }

    /**
     * [android.view.GestureDetector.OnGestureListener.onLongPress]
     */
    override fun onLongPress(e: MotionEvent?) {
    }

    /**
     * [android.view.ScaleGestureDetector.OnScaleGestureListener.onScaleBegin]
     */
    override fun onScaleBegin(e: ScaleGestureDetector?): Boolean {
        return true
    }

    /**
     * [android.view.ScaleGestureDetector.OnScaleGestureListener.onScaleEnd]
     */
    override fun onScaleEnd(detector: ScaleGestureDetector?) {
    }

    /**
     * [android.view.ScaleGestureDetector.OnScaleGestureListener.onScale]
     */
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val scale = detector.currentSpan / detector.previousSpan
        val values = FloatArray(9)
        mRenderMatrix.getValues(values)
        if (values[Matrix.MSCALE_X] * scale <= mScaleLimit) {
            // 縮小限界値で止める
            values[Matrix.MSCALE_X] = mScaleLimit
            values[Matrix.MSCALE_Y] = mScaleLimit
            mRenderMatrix.setValues(values)
        } else {
            mRenderMatrix.postScale(scale, scale, detector.focusX, detector.focusY)
        }
        updateTranslateLimit()
        renderBitmap()
        return true
    }

    /**
     * 縮小限界値を更新する
     */
    private fun updateScaleLimit() {
        if (::mRenderBitmap.isInitialized) {
            val widthLimit: Float = this.width.toFloat() / mRenderBitmap.width.toFloat()
            val heightLimit: Float = this.height.toFloat() / mRenderBitmap.height.toFloat()

            // 縮小限界値を画像の縦、横で短い方が画面サイズと一致するまでにする。
            // 最大は無限。
            mScaleLimit = if (widthLimit < heightLimit) widthLimit else heightLimit
        }
    }

    /**
     * 画像を移動する
     * @param distanceX 横方向の移動距離
     * @param distanceY 縦方向の移動距離
     */
    private fun translate(distanceX: Float, distanceY: Float) {
        val values = FloatArray(9)
        mRenderMatrix.getValues(values)
        if (values[Matrix.MSCALE_X] < mScaleLimit) {
            // 移動時に縮小限界値を超えている場合は、移動をキャンセルして縮小限界値(横幅いっぱい)に補正する。
            // 最小表示の状態で画面回転すると表示領域サイズが変わって縮小限界値を超えてしまうため。
            mRenderMatrix.setScale(mScaleLimit, mScaleLimit)
            updateTranslateLimit()
        } else {
            mRenderMatrix.postTranslate(distanceX, distanceY)
        }
        renderBitmap()
    }

    /**
     * 移動限界値を更新する
     */
    private fun updateTranslateLimit() {
        if (::mRenderBitmap.isInitialized) {
            val values = FloatArray(9)
            mRenderMatrix.getValues(values)
            val vwWidth: Float = this.width.toFloat()
            val vwHeight: Float = this.height.toFloat()
            val scaleWidth = mRenderBitmap.width.toFloat() * values[Matrix.MSCALE_X]
            val scaleHeight = mRenderBitmap.height.toFloat() * values[Matrix.MSCALE_Y]

            if (scaleWidth < vwWidth) {
                // 縮小で画面サイズを超える場合、画像が中央寄せで左右に余白ができるよう限界値を調整する
                mTranslateLimit.left = (vwWidth - scaleWidth) / 2
                mTranslateLimit.right = vwWidth - mTranslateLimit.left
            } else {
                mTranslateLimit.left = 0f
                mTranslateLimit.right = vwWidth - scaleWidth
            }
            if (scaleHeight < vwHeight) {
                // 縮小で画面サイズを超える場合、画像が中央寄せで上下に余白ができるよう限界値を調整する
                mTranslateLimit.top = (vwHeight - scaleHeight) / 2
                mTranslateLimit.bottom = vwHeight - mTranslateLimit.top
            } else {
                mTranslateLimit.top = 0f
                mTranslateLimit.bottom = vwHeight - scaleHeight
            }
        }
    }
}