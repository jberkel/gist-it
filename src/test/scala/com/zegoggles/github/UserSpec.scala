package com.zegoggles.github

import org.specs2.mutable._

class UserSpec extends Specification {
  "A user" should {
    "be parseable from JSON" in {
      val u = User.fromJSON("""
      {
        "user": {
          "gravatar_id": "b8dbb1987e8e5318584865f880036796",
          "company": "GitHub",
          "name": "Chris Wanstrath",
          "created_at": "2007/10/19 22:24:19 -0700",
          "location": "San Francisco, CA",
          "public_repo_count": 98,
          "public_gist_count": 270,
          "blog": "http://chriswanstrath.com/",
          "following_count": 196,
          "id": 2,
          "type": "User",
          "permission": null,
          "followers_count": 1692,
          "login": "defunkt",
          "email": "chris@wanstrath.com"
        }
      }
      """).get
      u.name must be equalTo "Chris Wanstrath"
      u.id must be equalTo 2
      u.login must be equalTo "defunkt"
      u.email must be equalTo "chris@wanstrath.com"
    }

    "should return None if not parseable" in {
      User.fromJSON("bla") must beNone
    }
  }
}