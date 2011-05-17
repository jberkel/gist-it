package com.zegoggles.github

import android.app.Activity
import actors.Futures
import android.os.{Handler, Bundle}
import com.zegoggles.github.Implicits._
import android.content.Intent

class UploadGist extends Activity with Logger with ApiActivity {
  val handler: Handler = new Handler();

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val intent = getIntent
    if (intent != null) {
      val text = intent.getStringExtra("android.intent.extra.TEXT")

      account.map { account =>
        val params = Map("description"->"a description",
            "public"->true,
            "files" ->Map("file1.txt"->Map("content"->text)))

        Futures.future {
          val resp = api.post(Request("https://api.github.com/users/"+account.name+"/gists")
                 .body(Api.map2Json(params).toString))

          resp.getStatusLine.getStatusCode match {
            case 201 =>
              val loc = resp.getFirstHeader("Location").getValue
              log("created: " + loc)
              handler.post {
                setResult(Activity.RESULT_OK, new Intent().putExtra("location", loc))
                finish()
              }
            case c   =>
              warn("status code ("+c+") " + resp.getStatusLine)
          }
        }
      }
    }
  }
}