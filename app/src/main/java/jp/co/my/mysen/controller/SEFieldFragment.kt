package jp.co.my.mysen.controller

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import jp.co.my.mygame.databinding.SeFragmentFieldBinding
import jp.co.my.mygame.toast
import jp.co.my.mysen.model.SEGameBalance
import jp.co.my.mysen.realm.*
import jp.co.my.mysen.view.SEFieldView
import jp.co.my.mysen.view.SESpeedChanger
import jp.co.my.mysen.view_model.SEFieldViewModel
import java.util.*

class SEFieldFragment: Fragment() {
    private lateinit var balance: SEGameBalance
    private var phase: Phase = Phase.FreeOrder
    private var day = 0 // 進行フェーズの現在日
    private var timer = Timer()
    private val mainHandler = Handler()

    private var _binding: SeFragmentFieldBinding? = null
    private val binding get() = _binding!!
    private lateinit var fieldView: SEFieldView
    private val viewModel : SEFieldViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SeFragmentFieldBinding.inflate(inflater, container, false)
        fieldView = binding.fieldView
        fieldView.listener = FieldViewListener()
        balance = SEGameBalance()

        updatePhaseButton()
        binding.dayProgressbar.max = balance.interfaceMaxDay
        binding.phaseButton.setOnClickListener {
            val nextPhase = when (phase) {
                is Phase.FreeOrder -> Phase.Advance
                is Phase.SelectDestination -> Phase.FreeOrder
                is Phase.Advance -> Phase.Pause
                is Phase.Pause -> Phase.Advance
            }
            setPhase(nextPhase)
        }
        binding.speedChanger.listener = object : SESpeedChanger.Listener {
            override fun onChangeSpeed() {
                if (phase is Phase.Advance) resetTimer()
            }
        }

        val realm = Realm.getDefaultInstance()
        if (realm.where<SEGeneralRealmObject>().count() == 0L
            || realm.where<SECountryRealmObject>().count() == 0L) {
            resetField()
        } else {
            setupField()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setPhase(nextPhase: Phase) {
        Log.d("tag", "setPhase $nextPhase")
        fieldView.clearHighlight()
        val prevPhase = phase
        phase = nextPhase
        when (prevPhase) {
            is Phase.FreeOrder -> {
                binding.dayProgressbar.progress = 0
            }
            is Phase.SelectDestination -> {
                if (prevPhase.units.first().destinationLand == null) {
                    // 出撃をキャンセル
                    Realm.getDefaultInstance().executeTransaction {
                        fieldView.enterUnits(prevPhase.units)
                    }
                }
            }
            is Phase.Advance -> timer.cancel()
            is Phase.Pause -> {
            }
        }

        when (nextPhase) {
            is Phase.SelectDestination -> {
            }
            is Phase.Advance -> {
                if (prevPhase is Phase.FreeOrder) {
                    day = 0
                }
                resetTimer()
            }
            is Phase.FreeOrder, is Phase.Pause -> {
            }
        }
        updatePhaseButton()
    }

    private fun updatePhaseButton() {
        binding.phaseButton.text = when (phase) {
            is Phase.FreeOrder -> "進行"
            is Phase.SelectDestination -> "戻る"
            is Phase.Advance -> "停止"
            is Phase.Pause -> "再開"
        }
    }

    private fun resetTimer() {
        val period = 1000 / binding.speedChanger.speedRatio()
        timer.cancel()
        timer = Timer()
        // BreakPointによる停止中にTimerが進まないように、1回毎にセット
        timer.schedule(object : TimerTask() {
            override fun run() {
                mainHandler.post { elapseDay() }
            }
        }, period)
    }

    private fun elapseDay() {
        Log.d("tag", "elapse day $day")
        if (phase !is Phase.Advance) throw IllegalArgumentException("Phase is not Advance")

        day++
        if (day <= balance.interfaceMaxDay) { // 最後の日付の後に1日分の時間猶予を作るため、timer実行回数はinterfaceMaxDayよりも1回多い
            binding.dayProgressbar.progress = day
            Realm.getDefaultInstance().executeTransaction {
                fieldView.moveAllUnit()
            }
            resetTimer()
            return
        }

        setPhase(Phase.FreeOrder)
    }

    private fun setupField() {
        binding.fieldView.initialize(balance, viewModel.loadObject(balance))
    }

    fun resetField() {
        Log.d("tag", "Start fetchModels")
        "Start fetchModels".toast(requireContext())
        viewModel.resetObject().observe(viewLifecycleOwner, { result: Boolean ->
            if (result) {
                "Fetch success".toast(requireContext())
                setupField()
            } else {
                "APIレスポンス取得に失敗".toast(requireContext())
            }
        })
    }

    private inner class FieldViewListener : SEFieldView.Listener {
        override fun onClickLand(land: SELandRealmObject) {
            fieldView.clearHighlight()
            when (val p = phase) {
                Phase.FreeOrder -> {
                    if (land.unitObjects.isEmpty()) {
                        val realm = Realm.getDefaultInstance()
                        realm.executeTransaction {
                            val unit = realm.createObject<SEUnitRealmObject>()
                            unit.startingLand = land
                            unit.currentLand = land
                            unit.general = realm.where<SEGeneralRealmObject>().findFirst()
                            fieldView.moveUnit(unit, land)
                            setPhase(Phase.SelectDestination(arrayListOf(unit)))
                        }
                        fieldView.highlightLands(listOf(land))
                    } else {
                        setPhase(Phase.SelectDestination(land.unitObjects))
                        fieldView.highlightLands(land.unitObjects.first()!!.remainingRouteLands()!!)
                    }
                }
                is Phase.SelectDestination -> {
                    if (p.units.first().destinationLand == land) {
                        // 2回目のタップで目標地点設定完了
                        setPhase(Phase.FreeOrder)
                    } else {
                        // 1回目のタップはルート表示のみ
                        Realm.getDefaultInstance().executeTransaction {
                            p.units.forEach { unit ->
                                SERouteRealmObject.bestRoute(unit, land, fieldView)?.also { route ->
                                    unit.route = route
                                    unit.destinationLand = land
                                    fieldView.highlightLands(route.lands)
                                } ?: run {
                                    Toast.makeText(fieldView.context, "移動不可", Toast.LENGTH_SHORT).show()
                                }
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

    sealed class Phase {
        // 命令フェーズ
        object FreeOrder : Phase()
        data class SelectDestination(val units: List<SEUnitRealmObject>) : Phase() // 目標選択

        // 進行フェーズ
        object Advance : Phase()
        object Pause : Phase() // 一時停止
    }
}