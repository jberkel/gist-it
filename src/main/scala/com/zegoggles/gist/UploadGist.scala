package com.zegoggles.gist
import Implicits._

import android.os.Bundle
import java.lang.Boolean
import android.widget.Toast
import android.net.Uri
import android.text.{TextUtils, ClipboardManager}
import android.content.{Intent, Context}
import android.app.{AlertDialog, ProgressDialog, Activity}
import android.view.{MenuItem, Menu, KeyEvent, View}
import actors.Futures
import org.apache.http.HttpStatus
import java.io.IOException
import android.view.inputmethod.{InputMethodManager, EditorInfo}

class UploadGist extends Activity with Logger with ApiActivity with TypedActivity {
  lazy val (public, filename, description, content) =
      (findView(TR.public_gist), findView(TR.filename), findView(TR.description), findView(TR.content))

  var previousGist:Option[Bundle] = None

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.upload_gist)
    setBackground(findView(TR.upload_root), R.drawable.octocat_bg, 15)

    content.requestFocus()
    content.setText(textFromIntent(getIntent))
    content.setOnEditorActionListener((v: View, id: Int, e: KeyEvent) =>
      if (id == EditorInfo.IME_ACTION_DONE)
        findViewById(R.id.upload_btn).performClick()
      else false
    )

    findView(TR.upload_btn).setOnClickListener { v: View =>
        hideSoftKeyboard(content)
        val doUpload = () => upload(previousGist, public.isChecked, filename, description, content)
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

    findView(TR.anon).setText(getString(R.string.anon_upload, getString(R.string.set_up_an_account)))
    Utils.clickify(findView(TR.anon), getString(R.string.set_up_an_account), addAccount(this))
  }

  override def onResume() {
    super.onResume()
    findView(TR.anon).setVisibility(if (account.isDefined) View.GONE else View.VISIBLE)
  }

  override def onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    previousGist.map(b => outState.putBundle(UploadGist.ExtraPreviousGist, b))
  }
  override def onRestoreInstanceState(state: Bundle) {
    super.onRestoreInstanceState(state)
    previousGist = if (state.getBundle(UploadGist.ExtraPreviousGist) != null)
      Some(state.getBundle(UploadGist.ExtraPreviousGist))
    else
      None
  }

  def upload(previous: Option[Bundle], public: Boolean, filename:String, description: String, content: String) = {
    val name = if (TextUtils.isEmpty(filename)) UploadGist.DefaultFileName else filename
    val params = Map(
      "description" -> description,
      "public"      -> public,
      "files"       -> Map(name -> Map("content" -> content)))

    val progress = ProgressDialog.show(this, null, getString(R.string.uploading), true)
    if (previous.isEmpty) {
      executeAsync(
        api.post(_),
        Request("/gists").body(params),
        HttpStatus.SC_CREATED, Some(progress))(onSuccess)(onError)
    } else {
      executeAsync(
        api.patch(_),
        Request("/gists/"+previous.get.getString("id")).body(params),
        HttpStatus.SC_OK, Some(progress))(onSuccess)(onError)
    }
  }

  def onSuccess(r: Api.Success) {
    Gist(r.getEntity).map { g =>
        Toast.makeText(this, R.string.gist_uploaded, Toast.LENGTH_SHORT).show()
        if (launchedViaIntent) {
          setResult(Activity.RESULT_OK, new Intent()
            .putExtra("location", g.url)
            .putExtra("url", g.public_url))
          finish()
        }
        copyToClipboard(g.public_url)
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

  def copyToClipboard(c: CharSequence) {
    getSystemService(Context.CLIPBOARD_SERVICE)
      .asInstanceOf[ClipboardManager]
      .setText(c)
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    if (resultCode == Activity.RESULT_OK) {
      requestCode match {
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
      case R.id.menu_clear     => clear(); true
      case _ => false
    }
  }

  def forkMe() {
    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_url))))
  }

  def clear() {
    List(content, filename, description).foreach(_.setText(""))
    public.setChecked(true)
    previousGist = None
  }

  def loadGist(intent:Intent) = Futures.future {
      try {
        val gistContent = io.Source.fromURL(intent.getData.toString).mkString
        copyToClipboard(gistContent)
        runOnUiThread { () =>
          Toast.makeText(this, R.string.gist_clipboard, Toast.LENGTH_SHORT).show()
          description.setText(intent.getStringExtra("description"))
          filename.setText(intent.getStringExtra("filename"))
          public.setChecked(intent.getBooleanExtra("public", true))
          content.setText(gistContent)
          previousGist = Some(intent.getExtras)
        }
      } catch {
        case e:IOException =>
          runOnUiThread(Toast.makeText(this,
            getString(R.string.loading_gist_failed, e.getMessage),
            Toast.LENGTH_SHORT).show()
          )
      }
  }

  def startLoadGist() {
    startActivityForResult(new Intent(this, classOf[GistList]), UploadGist.LoadRequest)
  }

  def setBackground(v: View, resId: Int, alpha: Int) {
    val bg = getResources.getDrawable(resId)
    bg.setAlpha(alpha)
    v.setBackgroundDrawable(bg)
  }

  def launchedViaIntent = getIntent != null && getIntent.getAction != Intent.ACTION_MAIN
  def hideSoftKeyboard(v:View) =
    getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
        .hideSoftInputFromWindow(v.getWindowToken, 0)
}

object UploadGist {
  val ExtraPreviousGist = "previousGist"
  val DefaultFileName   = "gistfile1.txt"
  val LoadRequest       = 1
}
