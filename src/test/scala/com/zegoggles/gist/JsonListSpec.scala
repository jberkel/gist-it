package com.zegoggles.gist

import org.specs2.mutable.Specification

class JsonListSpec extends Specification {
  "a valid json user list" should {
    "return a list of users" in {
      val l = """
      [
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
      },
      {
        "user": {
          "gravatar_id": "12345",
          "company": "GitHub",
          "name": "Foo Bar",
          "created_at": "2007/10/19 22:24:19 -0700",
          "location": "San Foo, CA",
          "public_repo_count": 98,
          "public_gist_count": 270,
          "blog": "http://chriswanstrath.com/",
          "following_count": 196,
          "id": 3,
          "type": "User",
          "permission": null,
          "followers_count": 1692,
          "login": "defunkt",
          "email": "chris@wanstrath.com"
        }
      }
      ]
      """
      val list = JsonList(l, User(_)).get
      list.size must be equalTo  2
    }
  }

  "an invalid list" should {
    "return None" in {
      JsonList("""invalid""", User(_)) must beNone
    }
  }
}