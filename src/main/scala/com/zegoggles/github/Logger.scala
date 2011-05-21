package com.zegoggles.github

import android.util.Log

trait Logger {
  def log(msg: String) { Log.d(App.TAG, msg) }
  def warn(msg: String, e:Exception*) {
      if (e.isEmpty)
        Log.w(App.TAG, msg)
      else
        Log.w(App.TAG, msg, e.headOption.get)
    }
}

trait StdoutLogger extends Logger {
    override def log(msg: String) {
        System.err.println(msg)
    }

    override def warn(msg: String, e:Exception*) {
        System.err.println(msg)
    }
}