package com.zegoggles.gist

import org.specs2.mutable.Specification
import org.specs2.matcher.MustThrownExpectations

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

    lazy val gist = Gist(valid_gist).get

    "be parseable from JSON" in {
      gist.id must be equalTo "1"
      gist.description must be equalTo "description of gist"
      gist.public must be equalTo true
      gist.url must be equalTo "https://api.github.com/gists/1"
      gist.size must be equalTo 932
      gist.filename must be equalTo "ring.erl"
      gist.raw_url must be equalTo "https://gist.github.com/raw/365370/8c4d2d43d178df44f4c03a7f2ac0ff512853564e/ring.erl"
      gist.content must be equalTo "contents of gist"
    }


    "have the last modified timestamp" in {
       gist.last_modified must be equalTo 1271204115
    }

    "show the last edit in a nice form" in {
      gist.last_modified_ago must be equalTo "1 year ago"
    }

    "have a public url" in {
      gist.public_url must be equalTo "https://gist.github.com/1"
    }

    "be pattern matchable" in {
      Gist(valid_gist) match {
        case Some(Gist("1", _, _, size, _, _, _, _, _)) => size must be equalTo 932
        case _    => error("default case")
      }
    }

    "have a method to access the backing content" in {
      gist.raw_content.get.size must be equalTo 932
    }

    "be parseable without content" in {
      val short_gist = """
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
          },
          "comments": 0,
          "git_pull_url": "git://gist.github.com/1.git",
          "git_push_url": "git@gist.github.com:1.git",
          "created_at": "2010-04-14T02:15:15Z"
        }
        """
      val gist = Gist(short_gist).get
      gist.id must be equalTo "1"
      gist.last_modified must be equalTo 1271204115
    }
  }

  "An invalid gist" should {
    "should return none" in {
      Gist("""invalid""") must beNone
    }
  }
}