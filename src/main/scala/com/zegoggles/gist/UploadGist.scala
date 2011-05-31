package com.zegoggles.gist
import Implicits._

import java.lang.Boolean
import android.widget.Toast
import android.net.Uri
import android.text.{TextUtils, ClipboardManager}
import android.view.{MenuItem, Menu, KeyEvent, View}
import org.apache.http.HttpStatus
import android.view.inputmethod.{InputMethodManager, EditorInfo}
import android.app.{Activity, AlertDialog, ProgressDialog}
import android.os.{BatteryManager, Bundle}
import android.content.{IntentFilter, BroadcastReceiver, Intent, Context}

class UploadGist extends Activity with Logger with ApiActivity with TypedActivity with BatteryAware {
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

    if (getIntent.getAction == Intents.UPLOAD_GIST) {
      // prefill some fields if we got called from our own intent
      public.setChecked(getIntent.getBooleanExtra(Extras.PUBLIC, true))
      description.setText(getIntent.getStringExtra(Extras.DESCRIPTION))
    }

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

    preloadGists()
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
            .setData(g.public_uri)
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

  def preloadGists() {
    // load gists eagerly if there's battery+connection
    if (isConnected && hasBattery)
      executeAsync(api.get(_),
        Request("/gists"),
        HttpStatus.SC_OK, None)(resp => app.preloadedList = Gist.list(resp.getEntity))(error => ())
  }

  def textFromIntent(intent: Intent):String = {
    if (intent == null)
        ""
    else if (intent.hasExtra(Intent.EXTRA_TEXT))
        intent.getStringExtra(Intent.EXTRA_TEXT)
    else if (intent.hasExtra(Intent.EXTRA_STREAM)) {
        val uri:Uri = intent getParcelableExtra(Intent.EXTRA_STREAM)
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
        val body = data.getStringExtra("body")
        if (!TextUtils.isEmpty(body)) {
          copyToClipboard(body)
          Toast.makeText(this, R.string.gist_clipboard, Toast.LENGTH_SHORT).show()
        }
        description.setText(data.getStringExtra(Extras.DESCRIPTION))
        filename.setText(data.getStringExtra(Extras.FILENAME))
        public.setChecked(data.getBooleanExtra(Extras.PUBLIC, true))
        content.setText(body)
        previousGist = Some(data.getExtras)
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

  def startLoadGist() {
    startActivityForResult(new Intent(Intents.PICK_GIST), 0)
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
}

object Extras {
  val DESCRIPTION = "description"
  val PUBLIC      = "public"
  val FILENAME    = "filename"
}

object Intents  {
  val PICK_GIST   = "com.zegoggles.gist.PICK"
  val UPLOAD_GIST = "com.zegoggles.gist.UPLOAD"
}


trait BatteryAware extends Activity {
  case class BatteryInfo(var level:Int, var scale:Int, var voltage:Int, var temp:Int) {}
  lazy val batteryStatus = new BatteryInfo(-1,-1,-1,-1)
  val receiver = new BroadcastReceiver() {
      override def onReceive(context:Context, intent:Intent) {
        batteryStatus.level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        batteryStatus.scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        batteryStatus.temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        batteryStatus.voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
      }
  }
  def hasBattery =  batteryStatus.level / batteryStatus.scale.toFloat > 0.7f
  override def onResume() {
    super.onResume()
    registerReceiver(receiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED))
  }

  override def onPause() {
    super.onPause()
    unregisterReceiver(receiver)
  }
}

package examples {
  // how to start from a console:
  // am start -a android.intent.action.MAIN -n com.zegoggles.gist/.examples.Upload
  class Upload extends Activity {
    override def onCreate(bundle: Bundle) {
      super.onCreate(bundle)

      // this is how you would call the upload activity from another app
      startActivityForResult(new Intent(Intents.UPLOAD_GIST)
          .putExtra(Intent.EXTRA_TEXT, "text123")
          .putExtra(Extras.PUBLIC, false)
          .putExtra(Extras.DESCRIPTION, "testing gist upload via intent"), 0)
    }

    override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
      if (resultCode == Activity.RESULT_OK && data.getData != null) {
        Toast.makeText(this, "Uploaded to "+data.getData,Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(this, "Canceled",Toast.LENGTH_SHORT).show();
      }
    }
  }
}