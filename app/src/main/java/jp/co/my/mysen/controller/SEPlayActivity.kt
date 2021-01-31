package jp.co.my.mysen.controller

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import jp.co.my.mygame.MainActivity
import jp.co.my.mygame.R
import jp.co.my.mygame.databinding.SePlayActivityBinding

class SEPlayActivity : AppCompatActivity() {
    private lateinit var binding: SePlayActivityBinding
    private lateinit var fragment: SEFieldFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SePlayActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fragment = SEFieldFragment()
        supportFragmentManager.beginTransaction().add(R.id.root_constraint, fragment).commit()
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
                fragment.fetchModels()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}