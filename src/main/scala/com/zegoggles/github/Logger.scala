package com.zegoggles.github

import android.util.Log

trait Logger {
    def log(msg: String) {
        Log.d("send-to-gist", msg)
    }
}

trait StdoutLogger extends Logger {
    override def log(msg: String) {
        System.err.println(msg)
    }
}