package com.zegoggles.gist
import Implicits._

import android.os.Bundle
import java.lang.Boolean
import android.view.{KeyEvent, View}
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import actors.Futures
import java.io.IOException
import scala.Either
import org.apache.http.{HttpResponse, HttpStatus}
import android.net.Uri
import android.text.{TextUtils, ClipboardManager}
import android.app.{AlertDialog, ProgressDialog, Activity}
import android.content.{Context, Intent}

class UploadGist extends Activity with Logger with ApiActivity with TypedActivity {

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.upload_gist)

    val bg = getResources.getDrawable(R.drawable.octocat_bg)
    bg.setAlpha(15)
    findView(TR.upload_root).setBackgroundDrawable(bg)

    val (public, description, content) = (findView(TR.public_gist), findView(TR.description), findView(TR.content))
    content.setText(textFromIntent(getIntent))
    content.setOnEditorActionListener((v: View, id: Int, e: KeyEvent) =>
      if (id == EditorInfo.IME_ACTION_DONE ||
        (e.getKeyCode == KeyEvent.KEYCODE_ENTER && e.getAction == KeyEvent.ACTION_DOWN))
        findViewById(R.id.upload_btn).performClick()
      else false)

    findViewById(R.id.upload_btn).setOnClickListener { v: View =>
        account.map { a =>
          val doUpload = () => upload(a.name, public.isChecked, description, content)
          if (!TextUtils.isEmpty(content))
            doUpload()
          else
            new AlertDialog.Builder(this)
              .setTitle(R.string.empty_gist_title)
              .setMessage(R.string.empty_gist)
              .setIcon(android.R.drawable.ic_dialog_alert)
              .setPositiveButton(android.R.string.ok, doUpload)
              .setNegativeButton(android.R.string.cancel, ()=>())
              .show()
        }
    }
  }

  def upload(user: String, public: Boolean, description: String, content: String) {
    val params = Map(
      "description" -> description,
      "public" -> public,
      "files" -> Map("file1.txt" -> Map("content" -> content)))

    val progress = new ProgressDialog(this)
    progress.setIndeterminate(true)
    progress.setMessage(getString(R.string.uploading))
    progress.show()

    executeAsync(api.post(_),
      Request("https://api.github.com/users/"+user+"/gists").body(params),
      HttpStatus.SC_CREATED) {
      success =>
        progress.dismiss()

        val gistUrl = success.getFirstHeader("Location").getValue
        log("created: " + gistUrl)
        val publicUrl = makePublicUrl(gistUrl)
        copyToClipboard(publicUrl)
        setResult(Activity.RESULT_OK, new Intent()
            .putExtra("location", gistUrl)
            .putExtra("url", publicUrl))
        finish()
    } {
      progress.dismiss()

      error => error match {
        case Left(exception) => warn("error", exception)
        case Right(resp) => warn("unexpected status code: " + resp.getStatusLine)
      }
      Toast.makeText(this, R.string.uploading_failed, Toast.LENGTH_LONG).show()
      finish()
    }
  }

  def executeAsync(call: Request => HttpResponse, req: Request, expected: Int)
                  (success: HttpResponse => Any)
                  (error: Either[IOException, HttpResponse] => Any) {
    Futures.future {
      try {
        val resp = call(req)
        resp.getStatusLine.getStatusCode match {
          case code if code == expected => onUiThread { success(resp)}
          case other => onUiThread { error(Right(resp))}
        }
      } catch {
        case e: IOException => onUiThread { error(Left(e)) }
      }
    }
  }

  def textFromIntent(intent: Intent):String = {
    if (intent == null)
        ""
    else if (intent.hasExtra(Intent.EXTRA_TEXT))
        intent.getStringExtra(Intent.EXTRA_TEXT)
    else if (intent.hasExtra(Intent.EXTRA_STREAM)) {
        val uri:Uri = intent getParcelableExtra(Intent.EXTRA_STREAM)
        log("fromIntent(uri="+uri+")")
        try {
          io.Source.fromInputStream(
            getContentResolver.openAssetFileDescriptor(uri, "r").createInputStream())
          .getLines().mkString
        } catch {
          case e:SecurityException =>
            Toast.makeText(this,
              getString(R.string.security_exception), Toast.LENGTH_LONG).show()
          ""
        }
    } else ""
  }

  def makePublicUrl(s: String) = "https://gist.github.com/" + Uri.parse(s).getLastPathSegment

  def copyToClipboard(c: CharSequence) {
    getSystemService(Context.CLIPBOARD_SERVICE)
      .asInstanceOf[ClipboardManager]
      .setText(c)
  }

  def onUiThread(f: => Unit) {
    runOnUiThread(new Runnable() {
      def run() {
        f
      }
    })
  }
}
