package jp.co.my.mysen

import android.os.Handler
import android.util.Log
import android.widget.Toast
import java.util.*

class SEUserInterface(private val balance: SEGameBalance, private val listener: Listener) {
    private var phase: Phase = Phase.FreeOrder
    private var day = 0 // 進行フェーズの現在日
    private var timer = Timer()
    private val mainHandler = Handler()
    private lateinit var fieldView: SEFieldView

    fun changePhaseByPlayer() {
        val nextPhase = when (phase) {
            is Phase.FreeOrder -> Phase.Advance
            is Phase.SelectDestination -> Phase.FreeOrder
            is Phase.Advance -> Phase.Pause
            is Phase.Pause -> Phase.Advance
        }
        setPhase(nextPhase)
    }

    private fun setPhase(nextPhase: Phase) {
        Log.d("tag", "setPhase $nextPhase")
        fieldView.clearHighlight()
        val prevPhase = phase
        phase = nextPhase
        when (prevPhase) {
            is Phase.SelectDestination -> {
                if (prevPhase.units.first().destinationLand == null) {
                    // 出撃をキャンセル
                    fieldView.enterUnits(prevPhase.units)
                }
            }
            is Phase.Advance -> timer.cancel()
            is Phase.FreeOrder, is Phase.Pause -> {
            }
        }

        when (nextPhase) {
            is Phase.SelectDestination -> {
            }
            is Phase.Advance -> {
                if (prevPhase is Phase.FreeOrder) {
                    day = 0
                }
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        mainHandler.post { elapseDay() }
                    }
                }, balance.interfaceIntervalSec * 1000, balance.interfaceIntervalSec * 1000)
            }
            is Phase.FreeOrder, is Phase.Pause -> {
            }
        }
        listener.onChangePhase(prevPhase, nextPhase)
    }

    fun changeButtonTitle() : String {
        return when (phase) {
            is Phase.FreeOrder -> "進行"
            is Phase.SelectDestination -> "戻る"
            is Phase.Advance -> "停止"
            is Phase.Pause -> "再開"
        }
    }

    private fun elapseDay() {
        Log.d("tag", "elapse day $day")
        day++
        if (day <= balance.interfaceMaxDay) { // 最後の日付の後に1日分の時間猶予を作るため、timer実行回数はinterfaceMaxDayよりも1回多い
            listener.onChangeDay(day)
            return
        }

        timer.cancel()
        setPhase(Phase.FreeOrder)
    }

    fun setField(fieldView: SEFieldView) {
        this.fieldView = fieldView
        fieldView.listener = FieldViewListener()
    }

    private inner class FieldViewListener : SEFieldView.Listener {
        override fun onClickLand(land: SELand) {
            fieldView.clearHighlight()
            when (val p = phase) {
                Phase.FreeOrder -> {
                    if (land.units.isEmpty()) {
                        val unit = SEUnit(land)
                        fieldView.moveUnit(unit, land)
                        setPhase(Phase.SelectDestination(arrayListOf(unit)))
                        fieldView.highlightLands(listOf(land))
                    } else {
                        setPhase(Phase.SelectDestination(land.units))
                        fieldView.highlightLands(land.units.first().route!!.lands)
                    }
                }
                is Phase.SelectDestination -> {
                    if (p.units.first().destinationLand == land) {
                        // 2回目のタップで目標地点設定完了
                        setPhase(Phase.FreeOrder)
                    } else {
                        // 1回目のタップはルート表示のみ
                        p.units.forEach { unit ->
                            SERouter(unit, land, fieldView).getBestRoute()?.also {
                                unit.route = it
                                unit.destinationLand = land
                                fieldView.highlightLands(it.lands)
                            } ?: run {
                                Toast.makeText(fieldView.context, "移動不可", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                Phase.Advance -> {
                }
                Phase.Pause -> {
                }
            }
        }
    }

    interface Listener {
        fun onChangePhase(prevPhase: Phase, nextPhase: Phase)
        fun onChangeDay(day: Int)
    }

    sealed class Phase {
        // 命令フェーズ
        object FreeOrder : Phase()
        data class SelectDestination(val units: List<SEUnit>) : Phase() // 目標選択

        // 進行フェーズ
        object Advance : Phase()
        object Pause : Phase() // 一時停止
    }
}