package jp.co.my.mysen.controller

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.kotlin.where
import jp.co.my.mygame.MainActivity
import jp.co.my.mygame.R
import jp.co.my.mygame.databinding.SePlayActivityBinding
import jp.co.my.mysen.model.SEGameBalance
import jp.co.my.mysen.model.SELand
import jp.co.my.mysen.realm.SEBaseRealmViewModel
import jp.co.my.mysen.realm.SECountryBaseRealm

class SEPlayActivity : AppCompatActivity() {
    private lateinit var binding: SePlayActivityBinding
    private lateinit var balance: SEGameBalance
    private lateinit var userInterface: SEUserInterface
    private val viewModel : SEBaseRealmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SePlayActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        balance = SEGameBalance()
        userInterface = SEUserInterface(balance, binding)

        val mockTypes = arrayOf(
            SELand.Type.Grass,
            SELand.Type.Grass,
            SELand.Type.Fort,
            SELand.Type.Highway,
            SELand.Type.Grass,
            SELand.Type.Grass,
            SELand.Type.Mountain,
            SELand.Type.Grass,
            SELand.Type.Highway,
            SELand.Type.Grass,
            SELand.Type.Grass,
            SELand.Type.Mountain,
            SELand.Type.Grass,
            SELand.Type.Highway,
            SELand.Type.Grass,
            SELand.Type.Grass,
            SELand.Type.Grass,
            SELand.Type.Highway,
            SELand.Type.Highway,
            SELand.Type.Grass,
            SELand.Type.Grass,
            SELand.Type.Grass,
            SELand.Type.Fort,
            SELand.Type.Grass,
            SELand.Type.Grass,
        )
        val lands = mockTypes.mapIndexed { index, type ->
            SELand(type, index % balance.fieldNumberOfX, index / balance.fieldNumberOfY)
        }
        binding.fieldView.initialize(balance, lands)

        loadRealm()
    }

    private fun loadRealm() {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            val countries = realm.where<SECountryBaseRealm>().findAll()
            Log.d("tag", countries.toString())
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
            R.id.menu_reset_realm -> {
                viewModel.createBaseRealms().observe(this, { result: Boolean ->
                    if (result) {
                        loadRealm()
                    } else {
                        Toast.makeText(this, "APIレスポンス取得に失敗", Toast.LENGTH_SHORT).show()
                    }
                })
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}