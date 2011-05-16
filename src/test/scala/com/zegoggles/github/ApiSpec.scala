package com.zegoggles.github

import com.github.jbrechtel.robospecs.RoboSpecs
import org.specs2.mutable._

class ApiSpec extends Specification {
    "the API" should {

        "parse the token response" in {
          val token = Api.parseTokenResponse("access_token=807e750b891b3fc47b0c951b4c11c0b610195b73&token_type=bearer")
          token must beSome
          token.get.access must be equalTo "807e750b891b3fc47b0c951b4c11c0b610195b73"
        }
    }
}
