package com.zegoggles.github

import android.graphics.Bitmap
import android.accounts.{Account, AccountManager, AccountAuthenticatorActivity}
import android.webkit._
import java.lang.String
import android.os.{Handler, Bundle}
import actors.Futures
import com.zegoggles.github.Implicits._
import android.webkit.WebSettings.ZoomDensity
import android.app.{AlertDialog, ProgressDialog}
import android.content.Context
import android.net.{ConnectivityManager, Uri}
import android.text.TextUtils
import org.apache.http.HttpStatus

class Login extends AccountAuthenticatorActivity with Logger with ApiActivity with TypedActivity {
  val handler = new Handler();
  lazy val view = findView(TR.webview)

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.login)

    view.getSettings.setDefaultZoom(ZoomDensity.FAR);
    view.getSettings.setJavaScriptEnabled(true)
    view.getSettings.setBlockNetworkImage(false)
    view.getSettings.setLoadsImagesAutomatically(true)

    val progress = new ProgressDialog(this);
    progress.setMessage(getString(R.string.loading_login))
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

      override def onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        showConnectionError(if (TextUtils.isEmpty(description)) None else Some(description))
      }

      override def onPageStarted(view: WebView, url: String, favicon: Bitmap) {
        super.onPageStarted(view, url, favicon)
        progress.show()
      }

      override def onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        view.loadUrl("javascript:document.getElementById('login_field').focus();")
        progress.dismiss()
      }
    })

    if (isConnected) {
      removeAllCookies()
      log("opening " + api.authorizeUrl)
      view.loadUrl(api.authorizeUrl)
    } else showConnectionError(None)
  }

  def exchangeToken(code: String) {
    api.exchangeToken(code).map {
        token =>
          log("successfully exchanged code for access token " + token)
          val resp = api.get("https://github.com/api/v2/json/user/show")
          resp.getStatusLine.getStatusCode match {
            case HttpStatus.SC_OK =>
              User.fromJSON(resp.getEntity).map { user =>
                handler.post {
                  setAccountAuthenticatorResult(
                    addAccount(user.login, token,
                      "id" -> user.id.toString,
                      "name" -> user.name,
                      "email" -> user.email))
                  finish()
                }
              }
            case c => log("invalid status ("+c+") "+resp.getStatusLine)
          }
      }
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

  def showConnectionError(message: Option[String]) {
    var error = getString(R.string.login_error_no_connection_message)
    message.map(m => error += " (" + m + ")")
    new AlertDialog.Builder(this)
      .setMessage(error)
      .setIcon(android.R.drawable.ic_dialog_alert)
      .setPositiveButton(android.R.string.ok, () => finish())
      .show()
  }

  def removeAllCookies() {
    CookieSyncManager.createInstance(this);
    CookieManager.getInstance().removeAllCookie();
  }

  def isConnected = {
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
    val info = manager.getActiveNetworkInfo
    info != null && info.isConnectedOrConnecting
  }

  override def onDestroy() {
    view.stopLoading();
    super.onDestroy()
  }
}

