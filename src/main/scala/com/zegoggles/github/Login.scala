package com.zegoggles.github

import android.graphics.Bitmap
import android.app.ProgressDialog
import android.net.Uri
import android.accounts.{Account, AccountManager, AccountAuthenticatorActivity}
import java.lang.String
import android.webkit._
import android.os.Bundle

class Login extends AccountAuthenticatorActivity with Logger with ApiActivity {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.login)

    val view: WebView = findViewById(R.id.webview).asInstanceOf[WebView]
    view.getSettings.setJavaScriptEnabled(true)
    view.getSettings.setBlockNetworkImage(false)
    view.getSettings.setLoadsImagesAutomatically(true)

    val progress = new ProgressDialog(this);
    progress.setIndeterminate(false);

    view.setWebViewClient(new LoggingWebViewClient() {
      override def shouldOverrideUrlLoading(view: WebView, url: String) = {
        super.shouldOverrideUrlLoading(view, url)
        if (url.startsWith(api.redirect_uri)) {
          val code = Uri.parse(url).getQueryParameter("code")
          log("code=" + code)
          val api = getApplication.asInstanceOf[App].api
          api.exchangeToken(code).map {
            token =>
              addAccount("unknown", accountType, token)
              val b = new Bundle()
              b.putString(AccountManager.KEY_ACCOUNT_NAME, "unknown")
              b.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType)
              setAccountAuthenticatorResult(b)
              finish()
          }
          true
        } else {
          false
        }
      }

      override def onPageStarted(view: WebView, url: String, favicon: Bitmap) {
        super.onPageStarted(view, url, favicon)
        progress.show()
      }

      override def onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        progress.hide()
      }
    })

    removeAllCookies()
    log("opening " + api.authorizeUrl)
    view.loadUrl(api.authorizeUrl)
  }

  def addAccount(name: String, atype: String, t: Token): Boolean = {
    val account = new Account(name, atype)
    val am = AccountManager.get(this)
    am.addAccountExplicitly(account, t.access, null)
  }

  def removeAllCookies() {
    CookieSyncManager.createInstance(this);
    CookieManager.getInstance().removeAllCookie();
  }
}