/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.versioning

import org.scalatest.{Matchers, OptionValues, TryValues, WordSpec}

class SbtVersioningSpec extends WordSpec with Matchers with TryValues with OptionValues {

  "SbtVersioning.updateTag" should {

    "return 0.1.0-1-g1234567 (stay the same) with a tag 0.1.0-1-g1234567" in {
      SbtGitVersioning.version("0.1.0-1-g1234567") shouldBe "0.1.0-1-g1234567"
    }

    "return 0.1.0-1-g1234567 when given v0.1.0-1-g1234567 (a tag with trailing 'v')" in {
      SbtGitVersioning.version("v0.1.0-1-g1234567") shouldBe "0.1.0-1-g1234567"
    }

    "return 0.1.0-0-g0000000 when given v0.1.0 (a tag with no added git-describe data)" in {
      SbtGitVersioning.version("v0.1.0") shouldBe "0.1.0-0-g0000000"
    }

    "return a release when TEST_MAKE_RELEASE is set" in new TestSetupForRelease {
      SbtGitVersioningForRelease.version("v0.1.0-1-g1234567") shouldBe "0.2.0"
    }

    "return the same release twice" in new TestSetupForRelease {
      SbtGitVersioningForRelease.version("0.2.0") shouldBe "0.2.0"
    }

    "support release/0.2.0. Legacy private services" in new TestSetupForRelease {
      SbtGitVersioningForRelease.version("release/0.2.0") shouldBe "0.2.0"
    }

    "support v0.2.0. Open services" in new TestSetupForRelease {
      SbtGitVersioningForRelease.version("v0.2.0") shouldBe "0.2.0"
    }

    "throw exception when given v0.1.0-SNAPSHOT (a tag with an incorrect format) when making a snapshot" in {
      val thrown = intercept[IllegalArgumentException] {
        SbtGitVersioning.version("v0.1.0-SNAPSHOT")
      }

      thrown.getMessage shouldBe "invalid version format for 'v0.1.0-SNAPSHOT'"
    }

    "throw exception when given v0.1.0-SNAPSHOT (a tag with an incorrect format) when making a release" in new TestSetupForRelease {
      val thrown = intercept[IllegalArgumentException] {
        SbtGitVersioningForRelease.version("v0.1.0-SNAPSHOT")
      }

      thrown.getMessage shouldBe "invalid version format for 'v0.1.0-SNAPSHOT'"
    }
  }

  trait TestSetupForRelease {
    val SbtGitVersioningForRelease = new SbtVersioning {
      override val makeReleaseEnvName: String = "TEST_MAKE_RELEASE"
    }
  }
}
