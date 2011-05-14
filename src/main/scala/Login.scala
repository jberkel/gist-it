package com.zegoggles.github

import android.os.Bundle
import android.graphics.Bitmap
import android.app.{ProgressDialog, Activity}
import java.lang.String
import android.net.Uri
import android.webkit.{CookieSyncManager, CookieManager, WebView, WebViewClient}
import android.accounts.AccountAuthenticatorActivity

object Login {
    val ClientId = "4d483ec8f7deecf9c6f3"
}

class Login extends AccountAuthenticatorActivity with Logger {
    override def onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val view: WebView = findViewById(R.id.webview).asInstanceOf[WebView]
        view.getSettings.setJavaScriptEnabled(true)

        val progress = new ProgressDialog(this);
        progress.setIndeterminate(false);

        view.setWebViewClient(new WebViewClient() {
            override def shouldOverrideUrlLoading(view: WebView, url: String) = {
                if (url.startsWith("http://callback")) {
                    val code = Uri.parse(url).getQueryParameter("code")
                    log("code=" + code)
                    val api = getApplication.asInstanceOf[App].api
                    api ! Exchange(code)

                    true
                } else {
                    false
                }
            }

            override def onPageStarted(view: WebView, url: String, favicon: Bitmap) {
                progress.show()
            }

            override def onPageFinished(view: WebView, url: String) {
                progress.hide()
            }
        })

        removeAllCookies()
        view.loadUrl("https://github.com/login/oauth/authorize?client_id=" +
                Login.ClientId + "&scope=gist&redirect_uri=http://callback/redirect")
    }

    def removeAllCookies() {
        CookieSyncManager.createInstance(this);
        CookieManager.getInstance().removeAllCookie();
    }
}