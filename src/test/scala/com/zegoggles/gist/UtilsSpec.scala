package com.zegoggles.gist

import org.specs2.mutable.Specification

class UtilsSpec extends Specification {
  "human readable size" should {
    "return human readable values" in {
      "323 bytes" must be equalTo Utils.humanReadableSize(323)
      "2 kb" must be equalTo Utils.humanReadableSize(2048)
      "1 mb" must be equalTo Utils.humanReadableSize(1024 * 1024 + 20)
      "3 gb" must be equalTo Utils.humanReadableSize(1024 * 1024 * 1024 * 3L)
    }
  }
}