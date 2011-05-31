package com.zegoggles.gist

import android.graphics.Bitmap
import java.lang.String
import android.webkit._
import android.os.Message
import android.net.http.SslError
import android.view.KeyEvent

class LoggingWebViewClient extends WebViewClient with Logger {
  override def shouldOverrideUrlLoading(view: WebView, url: String): Boolean = {
    log("shouldOverrideUrlLoading(" + url + ")")
    super.shouldOverrideUrlLoading(view, url)
  }

  override def onPageStarted(view: WebView, url: String, favicon: Bitmap) {
    log("onPageStarted(" + url + ")")
    super.onPageStarted(view, url, favicon)
  }

  override def onPageFinished(view: WebView, url: String) {
    log("onPageFinished(" + url + ")")
    super.onPageFinished(view, url)
  }

  override def onLoadResource(view: WebView, url: String) {
    log("onLoadResource(" + url + ")")
    super.onLoadResource(view, url)
  }

  override def onTooManyRedirects(view: WebView, cancelMsg: Message, continueMsg: Message) {
    log("onTooManyRedirects(" + cancelMsg + "," + continueMsg + ")")
    super.onTooManyRedirects(view, cancelMsg, continueMsg)
  }

  override def onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
    log("onReceivedError(" + errorCode + "," + description + "," + failingUrl + ")")
    super.onReceivedError(view, errorCode, description, failingUrl)
  }

  override def onFormResubmission(view: WebView, dontResend: Message, resend: Message) {
    log("onFormResubmission(" + dontResend + "," + resend + ")")
    super.onFormResubmission(view, dontResend, resend)
  }

  override def doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
    log("doUpdateVisitedHistory(" + url + "," + isReload + ")")
    super.doUpdateVisitedHistory(view, url, isReload)
  }

  override def onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
    log("onReceivedSslError(" + handler + "," + error + ")")
    super.onReceivedSslError(view, handler, error)
  }

  override def onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler, host: String, realm: String) {
    log("onReceivedHttpAuthRequest(" + handler + "," + host + "," + realm + ")")
    super.onReceivedHttpAuthRequest(view, handler, host, realm)
  }

  override def shouldOverrideKeyEvent(view: WebView, event: KeyEvent): Boolean = {
    log("shouldOverrideKeyEvent(" + event + ")")
    super.shouldOverrideKeyEvent(view, event)
  }

  override def onUnhandledKeyEvent(view: WebView, event: KeyEvent) {
    log("onUnhandledKeyEvent(" + event + ")")
    super.onUnhandledKeyEvent(view, event)
  }

  override def onScaleChanged(view: WebView, oldScale: Float, newScale: Float) {
    log("onScaleChanged(" + oldScale + "," + newScale + ")")
    super.onScaleChanged(view, oldScale, newScale)
  }
}


