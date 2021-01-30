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
import jp.co.my.mysen.realm.SECountryRealmObject
import jp.co.my.mysen.realm.SEGeneralRealmObject
import jp.co.my.mysen.realm.SELandRealmObject
import jp.co.my.mysen.view_model.SEBaseRealmViewModel

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

        val realm = Realm.getDefaultInstance()
        if (realm.where<SEGeneralRealmObject>().findAll().count() == 0
            || realm.where<SECountryRealmObject>().findAll().count() == 0) {
            fetchModels()
        } else {
            loadLands()
        }
    }

    private fun fetchModels() {
        Log.d("tag", "Start fetchModels")
        Toast.makeText(this, "Start fetchModels", Toast.LENGTH_SHORT).show()
        viewModel.createBaseRealms().observe(this, { result: Boolean ->
            if (result) {
                Toast.makeText(this, "Fetch success", Toast.LENGTH_SHORT).show()
                loadLands()
            } else {
                Toast.makeText(this, "APIレスポンス取得に失敗", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadLands() {
        val realm = Realm.getDefaultInstance()
        var lands: List<SELandRealmObject> = realm.where<SELandRealmObject>().findAll()
        if (0 < lands.count()) {
            Log.d("tag", lands.toString())
        } else {
            Log.d("tag", "Make new lands")
            val mockTypes = arrayOf(
                SELandRealmObject.Type.Grass,SELandRealmObject.Type.Grass,SELandRealmObject.Type.Fort,SELandRealmObject.Type.Highway,SELandRealmObject.Type.Grass,
                SELandRealmObject.Type.Grass,SELandRealmObject.Type.Mountain,SELandRealmObject.Type.Grass,SELandRealmObject.Type.Highway,SELandRealmObject.Type.Grass,
                SELandRealmObject.Type.Grass,SELandRealmObject.Type.Mountain,SELandRealmObject.Type.Grass,SELandRealmObject.Type.Highway,SELandRealmObject.Type.Grass,
                SELandRealmObject.Type.Grass,SELandRealmObject.Type.Grass,SELandRealmObject.Type.Highway,SELandRealmObject.Type.Highway,SELandRealmObject.Type.Grass,
                SELandRealmObject.Type.Grass,SELandRealmObject.Type.Grass,SELandRealmObject.Type.Fort,SELandRealmObject.Type.Grass,SELandRealmObject.Type.Grass,
            )
            lands = mockTypes.mapIndexed { index, type ->
                val land = SELandRealmObject()
                land.setup(type, index % balance.fieldNumberOfX, index / balance.fieldNumberOfY)
                land
            }
            realm.executeTransaction {
                realm.copyToRealm(lands)
            }
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
            R.id.menu_reset_realm -> {
                fetchModels()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}