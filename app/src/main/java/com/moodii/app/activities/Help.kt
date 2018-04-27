package com.moodii.app.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.*
import com.moodii.app.R
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_help.view.*


class Help : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        val browser = findViewById<WebView>(R.id.webviewHelp)

        browser.webViewClient = object: WebViewClient() {
            override fun onReceivedError(view:WebView, request: WebResourceRequest, error: WebResourceError) {
                Toast.makeText(this@Help, "Oops can't fetch remote help file ! " , Toast.LENGTH_LONG).show()
                view.loadUrl("about:blank")
            }
        }
        browser.loadUrl("http://www.moodii.com/help.html")
    }
}
