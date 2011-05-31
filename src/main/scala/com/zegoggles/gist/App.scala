package com.zegoggles.gist

class App extends android.app.Application with Logger with ApiHolder {
  var preloadedList:Option[Seq[Gist]] = None
}

object App {
  val TAG = "send-to-gist"
}