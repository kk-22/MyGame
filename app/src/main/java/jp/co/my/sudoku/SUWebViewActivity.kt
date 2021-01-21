package jp.co.my.sudoku

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import jp.co.my.mygame.databinding.SuWebviewActivityBinding
import org.jsoup.Jsoup
import kotlin.math.pow

class SUWebViewActivity: AppCompatActivity() {

    private lateinit var binding: SuWebviewActivityBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SuWebviewActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = WebViewClient() // リンクタップ時にWebView上で遷移させる
        binding.webView.addJavascriptInterface(this, "SUWebViewActivity")
        binding.webView.loadUrl("https://www.danboko.net/today.cgi")

        binding.backButton.setOnClickListener { binding.webView.goBack() }
        binding.forwardButton.setOnClickListener { binding.webView.goForward() }
        binding.importButton.setOnClickListener {
            binding.webView.loadUrl("javascript:window.SUWebViewActivity.viewSource(document.documentElement.outerHTML);")
        }
    }

    @JavascriptInterface
    fun viewSource(source: String) {
        val numbers = mutableListOf<String>()
        val maxCount = SUBoxTable.MAX_ROWS.toDouble().pow(2.0).toInt()
        val htmlBody = Jsoup.parse(source).normalise().body()
        val tableTag = htmlBody.getElementsByClass("trleft2").first()
        val imageTags = tableTag.getElementsByAttribute("src")
        imageTags.take(maxCount).forEachIndexed { index, imageTag ->
            val srcValue = imageTag.attributes().get("src")
            if (srcValue == "image/gray.png") {
                Log.d("tag", "パズルが表示されていない")
                return
            }
            when (srcValue) {
                "emage/white.png" -> numbers.add("")
                else -> {
                    numbers.add(srcValue.substring(7..7))
                }
            }
        }
        val intent = Intent()
        intent.putExtra("NUMBER_ARRAY", numbers.toTypedArray())
        setResult(RESULT_OK, intent)
        finish()
    }
}
