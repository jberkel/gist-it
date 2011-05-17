package com.zegoggles.github

import org.specs2.mutable._
class ApiSpec extends Specification {
    "the API" should {

        "parse the token response" in {
          val token = Api.parseTokenResponse("access_token=807e750b891b3fc47b0c951b4c11c0b610195b73&token_type=bearer")
          token must beSome
          token.get.access must be equalTo "807e750b891b3fc47b0c951b4c11c0b610195b73"
        }

      "return none if it cannot be parsed" in {
        Api.parseTokenResponse("foo=bar&token_type=bearer") must beNone
        Api.parseTokenResponse("") must beNone
      }

      "generate JSON from a Scala Map structure" in {
        val input = Map("foo"->"bar", "bool"->true, "int"->10,  "baz"->Map("buu"->"hello"),"double"->10.44d)
        """{"baz":{"buu":"hello"},"int":10,"foo":"bar","bool":true,"double":10.44}""" must
          be equalTo Api.map2Json(input).toString
      }
    }
}
