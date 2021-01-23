package jp.co.my.mysen

import android.os.Handler
import android.util.Log
import java.util.*

class SEUserInterface(private val balance: SEGameBalance, private val listener: UserInterfaceListener) {
    private var phase = Phase.Order
    private var day = 0 // 進行フェーズの現在日
    private var timer = Timer()
    private val mainHandler = Handler()

    fun changePhaseByPlayer() {
        setPhase(when (phase) {
            Phase.Order -> Phase.Advance
            Phase.Advance -> Phase.Pause
            Phase.Pause -> Phase.Advance
        })
    }

    private fun setPhase(nextPhase: Phase) {
        Log.d("tag", "setPhase $nextPhase")
        val prevPhase = phase
        phase = nextPhase
        timer.cancel()
        if (nextPhase == Phase.Advance) {
            if (prevPhase == Phase.Order) {
                day = 0
            }
            timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    mainHandler.post { elapseDay() }
                }
            }, balance.interfaceIntervalSec * 1000, balance.interfaceIntervalSec * 1000)
        }
        listener.didChangePhase(prevPhase, nextPhase)
    }

    fun changeButtonTitle() : String {
        return when (phase) {
            Phase.Order -> "進行"
            Phase.Advance -> "停止"
            Phase.Pause -> "再開"
        }
    }

    private fun elapseDay() {
        Log.d("tag", "elapse day $day")
        day++
        if (day <= balance.interfaceMaxDay) { // 最後の日付の後に1日分の時間猶予を作るため、timer実行回数はinterfaceMaxDayよりも1回多い
            listener.didChangeDay(day)
            return
        }

        timer.cancel()
        setPhase(Phase.Order)
    }

    enum class Phase {
        Order, // 命令フェーズ
        Advance, // 進行フェーズ
        Pause; // 一時停止
    }

    interface UserInterfaceListener {
        fun didChangePhase(prevPhase: Phase, nextPhase: Phase)
        fun didChangeDay(day: Int)
    }
}