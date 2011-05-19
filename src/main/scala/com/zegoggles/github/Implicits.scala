package com.zegoggles.github

import org.apache.http.HttpEntity
import io.Source
import android.view.View.OnClickListener
import android.view.{KeyEvent, View}
import android.widget.TextView.OnEditorActionListener
import android.widget.TextView
import org.json.JSONObject

object Implicits {
  implicit def block2Runnable(f: => Unit) = new Runnable {
    def run() { f }
  }

  implicit def map2JSON(map: Map[String,Any]):JSONObject = Api.map2Json(map)
  implicit def json2String(json: JSONObject):String = json.toString

  implicit def block2OnClickListener[F](f: View => F) = new OnClickListener {
    def onClick(v: View) { f(v) }
  }

  implicit def block2OnEditorActionListener(f: (View,Int,KeyEvent) => Boolean) = new OnEditorActionListener {
    def onEditorAction(v: TextView, actionId: Int, event: KeyEvent) = { f(v,actionId,event) }
  }

  implicit def entity2String(e: HttpEntity): String = {
    Source.fromInputStream(e.getContent).getLines().mkString
  }
}