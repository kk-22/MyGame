package jp.co.my.mygame

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import jp.co.my.mygame.databinding.ActivityMainBinding
import jp.co.my.mysen.SEPlayActivity
import jp.co.my.sudoku.SUPlayActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mysenButton.setOnClickListener {
            openActivity(SEPlayActivity::class.java)
        }
        binding.sudokuButton.setOnClickListener {
            openActivity(SUPlayActivity::class.java)
        }

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        pref.getString("PrevActivity", "")?.also {
            try {
                openActivity(Class.forName(it))
            } catch (e: Exception) {
            }
        }
    }

    private fun openActivity(cls: Class<*>) {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putString("PrevActivity", cls.canonicalName)
            .apply()

        val intent = Intent(applicationContext, cls)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }

    companion object {
        fun backToMain(context: Context) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove("PrevActivity")
                .apply()

            val intent = Intent(context, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
    }
}