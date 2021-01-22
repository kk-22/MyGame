package jp.co.my.mysen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import jp.co.my.mygame.databinding.SePlayActivityBinding

class SEPlayActivity : AppCompatActivity() {
    private lateinit var binding: SePlayActivityBinding
    private lateinit var balance: SEGameBalance
    private lateinit var fieldView: SEFieldView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SePlayActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        balance = SEGameBalance()
        fieldView = binding.fieldView
        fieldView.initialize(balance)
    }
}