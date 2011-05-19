package com.zegoggles.github

import android.util.Log

trait Logger {
    def log(msg: String) {
        Log.d("send-to-gist", msg)
    }

    def warn(msg: String) {
        Log.w("send-to-gist", msg)
    }

    def warn(msg: String, e:Exception) {
        Log.w("send-to-gist", msg, e)
    }

}

trait StdoutLogger extends Logger {
    override def log(msg: String) {
        System.err.println(msg)
    }

    override def warn(msg: String) {
        System.err.println(msg)
    }

}