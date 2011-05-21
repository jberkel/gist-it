package com.zegoggles.github

import org.apache.http.HttpEntity
import io.Source
import android.view.View.OnClickListener
import android.view.{KeyEvent, View}
import android.widget.TextView.OnEditorActionListener
import android.widget.TextView
import org.json.JSONObject
import android.content.DialogInterface

object Implicits {
  implicit def funToRunnable(f: => Unit) = new Runnable { def run() { f }}
  implicit def textViewToString(tv: TextView):String = tv.getText.toString
  implicit def mapToJSON(map: Map[String,Any]):JSONObject = Api.map2Json(map)
  implicit def jsonToString(json: JSONObject):String = json.toString

  implicit def funToDialogOnClickListener[F](f: (DialogInterface, Int) => F)
  = new DialogInterface.OnClickListener {
    def onClick(dialog: DialogInterface, which: Int) {
      f(dialog, which)
    }
  }

  implicit def funToDialogOnClickListener[F](f: () => F)
  = new DialogInterface.OnClickListener {
    def onClick(dialog: DialogInterface, which: Int) {
      f()
    }
  }

  implicit def funToOnClickListener[F](f: View => F) = new OnClickListener {
    def onClick(v: View) { f(v) }
  }

  implicit def funToOnEditorActionListener(f: (View,Int,KeyEvent) => Boolean) = new OnEditorActionListener {
    def onEditorAction(v: TextView, actionId: Int, event: KeyEvent) = { f(v,actionId,event) }
  }

  implicit def entityToString(e: HttpEntity): String =
    Source.fromInputStream(e.getContent).getLines().mkString
}