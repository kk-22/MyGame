package jp.co.my.mysen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import jp.co.my.mygame.databinding.SePlayActivityBinding
import jp.co.my.mysen.SELand.Type

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
    }
}