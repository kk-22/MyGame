package jp.co.my.mysen

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import jp.co.my.mygame.MainActivity
import jp.co.my.mygame.R
import jp.co.my.mygame.databinding.SePlayActivityBinding
import jp.co.my.mysen.SELand.Type

class SEPlayActivity : AppCompatActivity() {
    private lateinit var binding: SePlayActivityBinding
    private lateinit var balance: SEGameBalance
    private lateinit var userInterface: SEUserInterface
    private lateinit var fieldView: SEFieldView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SePlayActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        balance = SEGameBalance()
        binding.dayProgressbar.max = balance.interfaceMaxDay

        userInterface = SEUserInterface(balance, object : SEUserInterface.Listener {
            override fun onChangePhase(
                prevPhase: SEUserInterface.Phase,
                nextPhase: SEUserInterface.Phase
            ) {
                binding.phaseButton.text = userInterface.changeButtonTitle()
                if (prevPhase == SEUserInterface.Phase.Order) {
                    binding.dayProgressbar.progress = 0
                }
            }

            override fun onChangeDay(day: Int) {
                binding.dayProgressbar.progress = day
            }
        })
        fieldView = binding.fieldView

        val mockTypes = arrayOf(
            Type.Grass, Type.Grass, Type.Fort, Type.Highway, Type.Grass,
            Type.Grass, Type.Grass, Type.Grass, Type.Highway, Type.Grass,
            Type.Grass, Type.Grass, Type.Grass, Type.Highway, Type.Grass,
            Type.Grass, Type.Grass, Type.Highway, Type.Highway, Type.Grass,
            Type.Grass, Type.Grass, Type.Fort, Type.Grass, Type.Grass,
        )
        val lands = mockTypes.mapIndexed { index, type ->
            SELand(type, index % balance.fieldNumberOfX, index / balance.fieldNumberOfY)
        }
        fieldView.initialize(balance, lands)
        userInterface.setField(fieldView)

        binding.phaseButton.text = userInterface.changeButtonTitle()
        binding.phaseButton.setOnClickListener {
            userInterface.changePhaseByPlayer()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.se_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_back -> {
                MainActivity.backToMain(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}