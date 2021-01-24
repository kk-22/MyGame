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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SePlayActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        balance = SEGameBalance()
        userInterface = SEUserInterface(balance, binding)

        val mockTypes = arrayOf(
            Type.Grass, Type.Grass, Type.Fort, Type.Highway, Type.Grass,
            Type.Grass, Type.Mountain, Type.Grass, Type.Highway, Type.Grass,
            Type.Grass, Type.Mountain, Type.Grass, Type.Highway, Type.Grass,
            Type.Grass, Type.Grass, Type.Highway, Type.Highway, Type.Grass,
            Type.Grass, Type.Grass, Type.Fort, Type.Grass, Type.Grass,
        )
        val lands = mockTypes.mapIndexed { index, type ->
            SELand(type, index % balance.fieldNumberOfX, index / balance.fieldNumberOfY)
        }
        binding.fieldView.initialize(balance, lands)
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