package uk.gov.hmrc.versioning

import org.scalatest.{OptionValues, TryValues, Matchers, WordSpec}

class SbtVersioningSpec extends WordSpec with Matchers with TryValues with OptionValues {

  "SbtVersioning.updateTag" should {

    "return 0.1.0-1-g1234567 (stay the same) with a tag 0.1.0-1-g1234567" in {
      SbtGitVersioning.updateTag("0.1.0-1-g1234567") shouldBe "0.1.0-1-g1234567"
    }

    "return 0.1.0-1-g1234567 when given v0.1.0-1-g1234567 (a tag with trailing 'v')" in {
      SbtGitVersioning.updateTag("v0.1.0-1-g1234567") shouldBe "0.1.0-1-g1234567"
    }

    "return 0.1.0-0-g0000000 when given v0.1.0 (a tag with no added git-describe data)" in {
      SbtGitVersioning.updateTag("v0.1.0") shouldBe "0.1.0-0-g0000000"
    }

    "throw exception when given v0.1.0-SNAPSHOT (a tag with an incorrect format)" in {
      val thrown = intercept[IllegalArgumentException] {
        SbtGitVersioning.updateTag("v0.1.0-SNAPSHOT")
      }

      thrown.getMessage shouldBe "invalid version format for 'v0.1.0-SNAPSHOT'"
    }
  }
}
