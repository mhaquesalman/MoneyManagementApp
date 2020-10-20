package com.salman.moneymanagementapp.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import com.salman.moneymanagementapp.R
import kotlinx.android.synthetic.main.activity_website.*

class WebsiteActivity : AppCompatActivity() {
    val TAG = "WebsiteActivity"
    val URL = "https://www.google.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_website)

        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(URL)

    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}