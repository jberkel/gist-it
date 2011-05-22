package com.zegoggles.gist
import Implicits._

import android.os.Bundle
import java.lang.Boolean
import android.view.{KeyEvent, View}
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import org.apache.http.HttpStatus
import android.net.Uri
import android.text.{TextUtils, ClipboardManager}
import android.app.{AlertDialog, ProgressDialog, Activity}
import android.content.{Intent, Context}

class UploadGist extends Activity with Logger with ApiActivity with TypedActivity {

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
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

    findView(TR.upload_btn).setOnClickListener { v: View =>
        val doUpload = () => upload(public.isChecked, description, content)
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

    findView(TR.replace_btn).setOnClickListener { v:View =>
      startActivityForResult(new Intent(this, classOf[GistList]), 0)
    }

    findView(TR.anon).setText(getString(R.string.anon_upload, getString(R.string.set_up_an_account)))
    Utils.clickify(findView(TR.anon), getString(R.string.set_up_an_account), addAccount(this))
  }

  override def onResume() {
    super.onResume()
    findView(TR.anon).setVisibility(if (account.isDefined) View.GONE else View.VISIBLE)
  }

  def upload(public: Boolean, description: String, content: String) {
    val params = Map(
      "description" -> description,
      "public" -> public,
      "files" -> Map("file1.txt" -> Map("content" -> content)))

    val progress = ProgressDialog.show(this, null, getString(R.string.uploading), true)
    executeAsync(api.post(_),
      Request("https://api.github.com/gists").body(params),
      HttpStatus.SC_CREATED) {
      success =>
        progress.dismiss()

        val gistUrl = success.getFirstHeader("Location").getValue
        val publicUrl = makePublicUrl(gistUrl)
        copyToClipboard(publicUrl)

        log("gist uploaded to " + publicUrl)
        Toast.makeText(this, R.string.gist_uploaded, Toast.LENGTH_SHORT).show()

        if (getIntent != null && getIntent.getAction != Intent.ACTION_MAIN) {
          setResult(Activity.RESULT_OK, new Intent()
              .putExtra("location", gistUrl)
              .putExtra("url", publicUrl))
          finish()
        }
    } {
      progress.dismiss()

      error => error match {
        case Left(exception) => warn("error", exception)
        case Right(resp) => warn("unexpected status code: " + resp.getStatusLine)
      }
      Toast.makeText(this, R.string.uploading_failed, Toast.LENGTH_LONG).show()
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

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    if (resultCode == Activity.RESULT_OK) {
      val id = data.getLongExtra("id",-1)
      Toast.makeText(this, "Gist " +id+ " selected", Toast.LENGTH_LONG).show()
    }
  }
}
