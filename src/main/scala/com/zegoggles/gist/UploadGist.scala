package com.zegoggles.gist
import Implicits._

import android.os.Bundle
import java.lang.Boolean
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import android.net.Uri
import android.text.{TextUtils, ClipboardManager}
import android.content.{Intent, Context}
import org.apache.http.HttpStatus
import android.app.{AlertDialog, ProgressDialog, Activity}
import android.view.{MenuItem, Menu, KeyEvent, View}
import actors.Futures

class UploadGist extends Activity with Logger with ApiActivity with TypedActivity {
  lazy val (public, filename, description, content) =
      (findView(TR.public_gist), findView(TR.filename), findView(TR.description), findView(TR.content))

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.upload_gist)

    val bg = getResources.getDrawable(R.drawable.octocat_bg)
    bg.setAlpha(15)
    findView(TR.upload_root).setBackgroundDrawable(bg)

    content.setText(textFromIntent(getIntent))
    content.setOnEditorActionListener((v: View, id: Int, e: KeyEvent) =>
      if (id == EditorInfo.IME_ACTION_DONE ||
        (e.getKeyCode == KeyEvent.KEYCODE_ENTER && e.getAction == KeyEvent.ACTION_DOWN))
        findViewById(R.id.upload_btn).performClick()
      else false)

    findView(TR.upload_btn).setOnClickListener { v: View =>
        val doUpload = () => upload(public.isChecked, filename, description, content)
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
      startActivityForResult(new Intent(this, classOf[GistList]), UploadGist.ReplaceRequest)
    }

    findView(TR.anon).setText(getString(R.string.anon_upload, getString(R.string.set_up_an_account)))
    Utils.clickify(findView(TR.anon), getString(R.string.set_up_an_account), addAccount(this))
  }

  override def onResume() {
    super.onResume()
    findView(TR.anon).setVisibility(if (account.isDefined) View.GONE else View.VISIBLE)
    findView(TR.replace_btn).setVisibility(if (account.isDefined) View.VISIBLE else View.GONE)
  }

  def upload(public: Boolean, filename:String, description: String, content: String) {
    val name = if (TextUtils.isEmpty(filename)) UploadGist.DefaultFileName else filename
    val params = Map(
      "description" -> description,
      "public"      -> public,
      "files"       -> Map(name -> Map("content" -> content)))

    val progress = ProgressDialog.show(this, null, getString(R.string.uploading), true)
    executeAsync(api.post(_),
      Request("https://api.github.com/gists").body(params),
      HttpStatus.SC_CREATED, progress)(onSuccess)(onError)
  }

  def replace(data:Intent, public: Boolean, description: String, content: String) {
    val id = data.getStringExtra("id")
    log("replacing gist "+id)
    val params = Map(
        "public"      -> public,
        "files"       -> Map(data.getStringExtra("filename") -> Map("content" -> content)))
    val body = if (description.isEmpty) params else params ++ Map("description"->description)
    val progress = ProgressDialog.show(this, null, getString(R.string.uploading), true)

    executeAsync(api.patch(_),
      Request("https://api.gisthub.com/gists/"+id).body(body),
      HttpStatus.SC_OK, progress)(onSuccess)(onError)
  }

  def onSuccess(r: Api.Success) {
    val gistUrl = r.getFirstHeader("Location").getValue
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
  }

  def onError(error: Api.Error) {
    error match {
      case Left(exception) => warn("error", exception)
      case Right(resp) => warn("unexpected status code: " + resp.getStatusLine)
    }
    Toast.makeText(this, R.string.uploading_failed, Toast.LENGTH_LONG).show()
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
      requestCode match {
        case UploadGist.ReplaceRequest => replace(data, public.isChecked, description, content)
        case UploadGist.LoadRequest    => loadGist(data);
      }
    }
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    getMenuInflater.inflate(R.menu.menu, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem) = {
    item.getItemId match {
      case R.id.menu_fork_me   => forkMe();   true
      case R.id.menu_load_gist => startLoadGist(); true
      case _ => false
    }
  }

  def forkMe() {
    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_url))))
  }

  def loadGist(data:Intent) = Futures.future {
      copyToClipboard(io.Source.fromURL(data.getStringExtra("raw_url")).mkString)
      runOnUiThread(Toast.makeText(this, R.string.gist_clipboard, Toast.LENGTH_SHORT).show())
  }

  def startLoadGist() {
    startActivityForResult(new Intent(this, classOf[GistList]), UploadGist.LoadRequest)
  }
}

object UploadGist {
  val DefaultFileName = "gistfile1.txt"
  val LoadRequest    = 1
  val ReplaceRequest = 2
}
