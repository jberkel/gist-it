package com.zegoggles.github

import android.graphics.Bitmap
import android.app.ProgressDialog
import android.net.Uri
import android.accounts.{Account, AccountManager, AccountAuthenticatorActivity}
import android.webkit._
import java.lang.String
import org.json.JSONObject
import android.os.{Handler, Bundle}
import actors.Futures
import com.zegoggles.github.Implicits._

class Login extends AccountAuthenticatorActivity with Logger with ApiActivity {
  val handler: Handler = new Handler();
  lazy val view: WebView = findViewById(R.id.webview).asInstanceOf[WebView]

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.login)

    view.getSettings.setJavaScriptEnabled(true)
    view.getSettings.setBlockNetworkImage(false)
    view.getSettings.setLoadsImagesAutomatically(true)

    val progress = new ProgressDialog(this);
    progress.setIndeterminate(false);

    view.setWebViewClient(new LoggingWebViewClient() {
      override def shouldOverrideUrlLoading(view: WebView, url: String) = {
        super.shouldOverrideUrlLoading(view, url)
        if (url.startsWith(api.redirect_uri)) {
          Uri.parse(url).getQueryParameter("code") match {
            case code:String => Futures.future { exchangeToken(code) }
            case _ => warn("no code found in redirect uri")
          }
          true
        } else false
      }

      def exchangeToken(code: String) {
        api.exchangeToken(code).map {
          token =>
            log("successfully exchanged code for access token " + token)
            val resp = api.get("https://github.com/api/v2/json/user/show")
            resp.getStatusLine.getStatusCode match {
              case 200 =>
                val json:String = resp.getEntity
                log("got " + json)
                val user = User.fromJSON(new JSONObject(json).getJSONObject("user"))
                handler.post {
                  setAccountAuthenticatorResult(
                    addAccount(user.login, token,
                      "id" -> user.id.toString,
                      "name" -> user.name,
                      "email" -> user.email))
                  finish()
                }
              case c => log("invalid status ("+c+") "+resp.getStatusLine)
            }
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

  def addAccount(name: String, token: Token, data: (String, String)*): Bundle = {
    val account = new Account(name, accountType)
    val am = AccountManager.get(this)
    am.addAccountExplicitly(account, token.access, null)
    am.setAuthToken(account, "access", token.access);
    for ((k, v) <- data) am.setUserData(account, k, v)
    val b = new Bundle()
    b.putString(AccountManager.KEY_ACCOUNT_NAME, name)
    b.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType)
    b
  }

  def removeAllCookies() {
    CookieSyncManager.createInstance(this);
    CookieManager.getInstance().removeAllCookie();
  }

  override def onDestroy() {
    view.stopLoading();
    super.onDestroy()
  }
}

