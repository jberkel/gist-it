package com.zegoggles.gist

import org.specs2.mutable.Specification
import org.specs2.matcher.MustExpectable._

class GistSpec extends Specification {

  "A valid gist" should {
       val valid_gist = """
        {
          "url": "https://api.github.com/gists/1",
          "id": "1",
          "description": "description of gist",
          "public": true,
          "user": {
            "login": "octocat",
            "id": 1,
            "gravatar_url": "https://github.com/images/error/octocat_happy.gif",
            "url": "https://api.github.com/users/octocat"
          },
          "files": {
            "ring.erl": {
              "size": 932,
              "filename": "ring.erl",
              "raw_url": "https://gist.github.com/raw/365370/8c4d2d43d178df44f4c03a7f2ac0ff512853564e/ring.erl",
              "content": "contents of gist"
            }
          },
          "comments": 0,
          "git_pull_url": "git://gist.github.com/1.git",
          "git_push_url": "git@gist.github.com:1.git",
          "created_at": "2010-04-14T02:15:15Z",
          "forks": [
            {
              "user": {
                "login": "octocat",
                "id": 1,
                "gravatar_url": "https://github.com/images/error/octocat_happy.gif",
                "url": "https://api.github.com/users/octocat"
              },
              "url": "https://api.github.com/gists/5",
              "created_at": "2011-04-14T16:00:49Z"
            }
          ],
          "history": [
            {
              "url": "https://api.github.com/gists/1/57a7f021a713b1c5a6a199b54cc514735d2d462f",
              "version": "57a7f021a713b1c5a6a199b54cc514735d2d462f",
              "user": {
                "login": "octocat",
                "id": 1,
                "gravatar_url": "https://github.com/images/error/octocat_happy.gif",
                "url": "https://api.github.com/users/octocat"
              },
              "change_status": {
                "deletions": 0,
                "additions": 180,
                "total": 180
              },
              "committed_at": "2010-04-14T02:15:15Z"
            }
          ]
        }
      """

    "be parseable from JSON" in {
      val g = Gist(valid_gist).get
      g.id must be equalTo "1"
      g.description must be equalTo "description of gist"
      g.public must be equalTo true
      g.url must be equalTo "https://api.github.com/gists/1"
      g.size must be equalTo 932
      g.filename must be equalTo "ring.erl"
      g.raw_url must be equalTo "https://gist.github.com/raw/365370/8c4d2d43d178df44f4c03a7f2ac0ff512853564e/ring.erl"
      g.content must be equalTo "contents of gist"
      g.describe must be equalTo "ring.erl (932 bytes)"
    }

    "have a method to access the backing content" in {
      Gist(valid_gist).get.raw_content.get.size must be equalTo 932
    }

    "be parseable without content" in {
      val gist = """
        {
          "url": "https://api.github.com/gists/1",
          "id": "1",
          "description": "description of gist",
          "public": true,
          "user": {
            "login": "octocat",
            "id": 1,
            "gravatar_url": "https://github.com/images/error/octocat_happy.gif",
            "url": "https://api.github.com/users/octocat"
          },
          "files": {
            "ring.erl": {
              "size": 932,
              "filename": "ring.erl",
              "raw_url": "https://gist.github.com/raw/365370/8c4d2d43d178df44f4c03a7f2ac0ff512853564e/ring.erl",
            }
          }
        }
        """
      Gist(gist).get.id must be equalTo "1"
    }
  }


  "An invalid gist" should {
    "should return none" in {
      Gist("""invalid""") must beNone
    }
  }
}