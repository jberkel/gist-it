package com.zegoggles.github

import org.apache.http.HttpEntity
import io.Source

object Implicits {
  implicit def block2Runnable[F](f: => F): Runnable = new Runnable {
    def run() {
      f
    }
  }

  implicit def entity2String(e: HttpEntity): String = {
    Source.fromInputStream(e.getContent).getLines().mkString
  }
}