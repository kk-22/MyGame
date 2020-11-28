package jp.co.my.mygame

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import jp.co.my.mygame.databinding.SuWebviewActivityBinding

class SUWebViewActivity: AppCompatActivity() {

    private lateinit var binding: SuWebviewActivityBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SuWebviewActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webView.loadUrl("https://www.danboko.net/today.cgi")
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = WebViewClient() // リンクタップ時に外部アプリ開かない
    }
}
