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
import jp.co.my.mysen.realm.*
import jp.co.my.mysen.view.SEFieldView
import jp.co.my.mysen.view.SESpeedChanger
import jp.co.my.mysen.view_model.SEFieldViewModel
import java.util.*

class SEFieldFragment: Fragment() {
    private lateinit var playerObject: SEPlayerRealmObject
    private var phase: Phase = Phase.FreeOrder
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

        updatePhaseButton()
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
        timer.cancel()
    }

    private fun setPhase(nextPhase: Phase) {
        Log.d("tag", "setPhase $nextPhase")
        fieldView.clearHighlight()
        val prevPhase = phase
        phase = nextPhase
        when (prevPhase) {
            is Phase.FreeOrder -> {
            }
            is Phase.SelectDestination -> {
            }
            is Phase.Advance -> timer.cancel()
            is Phase.Pause -> {
            }
        }

        when (nextPhase) {
            is Phase.SelectDestination -> {
            }
            is Phase.Advance -> {
                resetTimer()
            }
            is Phase.FreeOrder, is Phase.Pause -> {
            }
        }
        updatePhaseButton()
    }

    private fun updatePhaseButton() {
        binding.phaseButton.text = when (phase) {
            is Phase.FreeOrder -> "??????"
            is Phase.SelectDestination -> "??????"
            is Phase.Advance -> "??????"
            is Phase.Pause -> "??????"
        }
    }

    private fun resetTimer() {
        val period = 1000 / binding.speedChanger.speedRatio()
        timer.cancel()
        timer = Timer()
        // BreakPoint?????????????????????Timer???????????????????????????1??????????????????
        timer.schedule(object : TimerTask() {
            override fun run() {
                mainHandler.post { elapseDay() }
            }
        }, period)
    }

    private fun elapseDay() {
        Log.d("tag", "elapse day ${playerObject.currentDay}")
        if (phase !is Phase.Advance) throw IllegalArgumentException("Phase is not Advance")

        Realm.getDefaultInstance().executeTransaction {
            playerObject.currentDay++
            if (playerObject.currentDay <= playerObject.interfaceMaxDay) { // ????????????????????????1???????????????????????????????????????timer???????????????interfaceMaxDay?????????1?????????
                fieldView.moveAllUnit()
                resetTimer()
            } else {
                playerObject.currentDay = 0
                setPhase(Phase.FreeOrder)
            }
            binding.dayProgressbar.progress = playerObject.currentDay
        }
    }

    private fun setupField() {
        Realm.getDefaultInstance().executeTransaction { realm ->
            realm.where<SEPlayerRealmObject>().findFirst()?.also { player ->
                playerObject = player
            } ?: run {
                playerObject = realm.createObject<SEPlayerRealmObject>()
            }
        }
        binding.dayProgressbar.progress = playerObject.currentDay
        binding.dayProgressbar.max = playerObject.interfaceMaxDay

        binding.fieldView.initialize(playerObject, viewModel.loadObject(playerObject))
    }

    fun resetField() {
        Log.d("tag", "Start fetchModels")
        "Start fetchModels".toast(requireContext())
        viewModel.resetObject().observe(viewLifecycleOwner, { result: Boolean ->
            if (result) {
                "Fetch success".toast(requireContext())
                setupField()
            } else {
                "API??????????????????????????????".toast(requireContext())
            }
        })
    }

    private inner class FieldViewListener : SEFieldView.Listener {
        override fun onClickLand(land: SELandRealmObject) {
            binding.infoStackView.updateLand(land)

            fieldView.clearHighlight()
            when (val p = phase) {
                Phase.FreeOrder -> {
                    if (!land.type.isBase()) {
                        "?????????????????????????????????".toast(context!!)
                    } else if (!land.governingCountry!!.isPlayerCountry) {
                        ("CPU???????????????").toast(context!!)
                    } else if (land.unitObjects.isEmpty()) {
                        val realm = Realm.getDefaultInstance()
                        realm.executeTransaction {
                            val unit = realm.createObject<SEUnitRealmObject>()
                            unit.startingLand = land
                            unit.currentLand = land
                            unit.destinationLand = land
                            unit.route = SERouteRealmObject.firstRoute(unit)
                            unit.general = realm.where<SEGeneralRealmObject>().findFirst()
                            fieldView.moveUnit(unit, land)
                            setPhase(Phase.SelectDestination(arrayListOf(unit)))
                        }
                        fieldView.highlightLands(listOf(land))
                    } else {
                        setPhase(Phase.SelectDestination(land.unitObjects))
                        val unit = land.unitObjects.first()
                        val lands = unit!!.remainingRouteLands()
                         fieldView.highlightLands(lands!!)
                    }
                }
                is Phase.SelectDestination -> {
                    if (p.units.first().destinationLand == land) {
                        // 2?????????????????????????????????????????????
                        setPhase(Phase.FreeOrder)
                    } else {
                        // 1??????????????????????????????????????????
                        Realm.getDefaultInstance().executeTransaction {
                            p.units.forEach { unit ->
                                SERouteRealmObject.bestRoute(unit, land, fieldView)?.also { route ->
                                    unit.route = route
                                    unit.destinationLand = land
                                    fieldView.highlightLands(route.lands)
                                } ?: run {
                                    Toast.makeText(fieldView.context, "????????????", Toast.LENGTH_SHORT).show()
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
        // ??????????????????
        object FreeOrder : Phase()
        data class SelectDestination(val units: List<SEUnitRealmObject>) : Phase() // ????????????

        // ??????????????????
        object Advance : Phase()
        object Pause : Phase() // ????????????
    }
}