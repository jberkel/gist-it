package com.zegoggles.github

import android.os.Bundle
import com.zegoggles.github.Implicits._
import java.lang.Boolean
import android.view.{KeyEvent, View}
import android.view.inputmethod.EditorInfo
import android.app.{ProgressDialog, Activity}
import android.widget.{Toast, EditText, CheckBox}
import actors.Futures
import android.text.ClipboardManager
import android.content.{Context, Intent}
import java.io.IOException
import scala.Either
import org.apache.http.{HttpResponse, HttpStatus}
import android.net.Uri

class UploadGist extends Activity with Logger with ApiActivity {
    override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.upload_gist)

    val intent = getIntent
    if (intent != null) {
      val text = intent.getStringExtra("android.intent.extra.TEXT")
      val public: CheckBox = findViewById(R.id.public_gist).asInstanceOf[CheckBox]
      val description: EditText = findViewById(R.id.description).asInstanceOf[EditText]

      description.setOnEditorActionListener((v:View, id:Int, e:KeyEvent) =>
        if (id == EditorInfo.IME_ACTION_DONE || 
           (e.getKeyCode == KeyEvent.KEYCODE_ENTER && e.getAction == KeyEvent.ACTION_DOWN))
          findViewById(R.id.ok).performClick()
        else false)

      findViewById(R.id.ok).setOnClickListener { v: View =>
          account.map(a =>
              upload(a.name, public.isChecked, description.getText.toString, text)
          )
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
    progress.setMessage("Uploading...")
    progress.show()

    executeAsync(api.post(_),
        Request("https://api.github.com/users/" + user + "/gists").body(params),
        HttpStatus.SC_CREATED)
      { success =>
        val gistUrl = success.getFirstHeader("Location").getValue
        log("created: " + gistUrl)
        copyToClipboard(makePublicUrl(gistUrl))
        progress.dismiss()
        setResult(Activity.RESULT_OK, new Intent().putExtra("location", gistUrl))
        finish()
      }
      { error => error match {
          case Left(exception) => warn("error", exception)
          case Right(resp)     => warn("status code: " + resp.getStatusLine)
        }
        progress.dismiss()
        Toast.makeText(this, "Uploading failed", Toast.LENGTH_LONG).show()
        finish()
      }
  }

  def executeAsync(call: Request => HttpResponse, req: Request, expected: Int)
        (success: HttpResponse => Any)
        (error:   Either[IOException,HttpResponse] => Any) {
    Futures.future {
      try {
        val resp = call(req)

        resp.getStatusLine.getStatusCode match {
          case code if code == expected  => onUiThread { success(resp) }
          case other                     => onUiThread { error(Right(resp)) }
        }
      } catch {
        case e:IOException => onUiThread { error(Left(e)) }
      }
    }
  }

  def makePublicUrl(s: String): CharSequence =
    "https://gist.github.com/"+Uri.parse(s).getLastPathSegment

  def copyToClipboard(c: CharSequence) {
    getSystemService(Context.CLIPBOARD_SERVICE)
      .asInstanceOf[ClipboardManager]
      .setText(c);
  }

  def onUiThread(f: => Unit) {
    runOnUiThread(new Runnable() {
      def run() {
        f
      }
    })
  }
}
