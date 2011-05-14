package com.zegoggles.github

import com.github.jbrechtel.robospecs.RoboSpecs
import org.specs2.mutable._

class ApiSpec extends Specification with RoboSpecs {

    "the API" should {
        "exchange a code for a token" in {
            val api = new Api("foo", "bar", "whatever")
            val token = api.exchangeToken("code")
            token.access must not be empty
        }
    }
}
