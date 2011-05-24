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

  "human readable time" should {
    "return human readable values" in {
      "50 seconds" must be equalTo Utils.humanTime(50)
      "3 minutes" must be equalTo Utils.humanTime(190)
      "1 hour" must be equalTo Utils.humanTime(3800)
      "4 days" must be equalTo Utils.humanTime(86400 * 4 + 3000)
      "2 months" must be equalTo Utils.humanTime(86400 * 62)
      "1 year" must be equalTo Utils.humanTime(86400 * 30 * 12)
    }
  }
}