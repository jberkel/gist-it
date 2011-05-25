package com.zegoggles.gist

import org.specs2.mutable.Specification

class UtilsSpec extends Specification {
  "human readable size" should {
    "return human readable values" in {
      "323 bytes" must be equalTo Utils.readableSize(323)
      "2 kb" must be equalTo Utils.readableSize(2048)
      "1 mb" must be equalTo Utils.readableSize(1024 * 1024 + 20)
      "3 gb" must be equalTo Utils.readableSize(1024 * 1024 * 1024 * 3L)
    }
  }

  "human readable time" should {
    "return human readable values" in {
      "just now" must be equalTo Utils.readableTime(5)
      "50 seconds ago" must be equalTo Utils.readableTime(50)
      "3 minutes ago" must be equalTo Utils.readableTime(190)
      "1 hour ago" must be equalTo Utils.readableTime(3800)
      "4 days ago" must be equalTo Utils.readableTime(86400 * 4 + 3000)
      "2 months ago" must be equalTo Utils.readableTime(86400 * 62)
      "1 year ago" must be equalTo Utils.readableTime(86400 * 30 * 12)
    }
  }
}