package com.zegoggles.gist

import android.accounts.{Account, OnAccountsUpdateListener, AccountManager}
import org.acra.annotation.ReportsCrashes
import org.acra.{ReportingInteractionMode, ACRA}

@ReportsCrashes(formKey = "dFpjeHlmU1NGM1J5NjNhWlpyQWpqWVE6MQ",
  mode = ReportingInteractionMode.NOTIFICATION,
  resNotifTickerText = R.string.crash_notif_ticker_text,
  resNotifTitle = R.string.crash_notif_title,
  resNotifText = R.string.crash_notif_text,
  resDialogText = R.string.crash_dialog_text,
  resDialogTitle = R.string.crash_dialog_title,
  resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
  resDialogOkToast = R.string.crash_dialog_ok_toast)
class App extends android.app.Application with Logger with ApiHolder {
  var preloadedList:Option[Seq[Gist]] = None

  override def onCreate() {
    ACRA.init(this)

    // reload current token and clear list when accounts get changed
    AccountManager.get(this).addOnAccountsUpdatedListener(new OnAccountsUpdateListener {
      def onAccountsUpdated(accounts: Array[Account]) {
        api.token     = token
        preloadedList = None
      }
    }, /*handler*/ null, /*updateImmediately*/ false)
  }
}

object App {
  val TAG = "send-to-gist"
}