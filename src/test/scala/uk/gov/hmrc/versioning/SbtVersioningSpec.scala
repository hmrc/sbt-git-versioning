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

  "SbtVersioning.version - when making a SNAPSHOT" should {

    "return 0.2.0-SNAPSHOT when git describe is v0.1.0-1-g1234567" in new TestSetupForSnapshot {
      SbtGitVersioningForSnapshot.version("v0.1.0-1-g1234567", 0) shouldBe "0.2.0-SNAPSHOT"
    }

    "return 0.2.0-SNAPSHOT when git describe is v0.1.0 (a tag on HEAD)" in new TestSetupForSnapshot {
      SbtGitVersioningForSnapshot.version("v0.1.0", 0) shouldBe "0.2.0-SNAPSHOT"
    }

    "throw exception when given v0.1.0.1 (a tag with an incorrect format)" in new TestSetupForSnapshot {
      intercept[IllegalArgumentException] {
        SbtGitVersioningForSnapshot.version("v0.1.0.1", 0)
      }.getMessage shouldBe "invalid version format for 'v0.1.0.1'"
    }

    "use the new major version" in new TestSetupForSnapshot {
      SbtGitVersioningForSnapshot.version("v0.1.0-1-g1234567", 1) shouldBe "1.0.0-SNAPSHOT"
    }

    "use the new major version and return 1.0.0-SNAPSHOT when given v0.1.0 (a tag on HEAD)" in new TestSetupForSnapshot {
      SbtGitVersioningForSnapshot.version("v0.1.0", 1) shouldBe "1.0.0-SNAPSHOT"
    }
  }

  "SbtVersioning.version - when making a RELEASE (MAKE_RELEASE env var is set)" should {

    "return a release when TEST_MAKE_RELEASE is set" in new TestSetupForRelease {
      SbtGitVersioningForRelease.version("v0.1.0-1-g1234567", 0) shouldBe "0.2.0"
    }

    "always increment the minor version - major version is not specified" in new TestSetupForRelease {
      SbtGitVersioningForRelease.version("v0.2.0", 0) shouldBe "0.3.0"
    }

    "support release/0.2.0 - Legacy private services" in new TestSetupForRelease {
      SbtGitVersioningForRelease.version("release/0.2.0", 0) shouldBe "0.3.0"
    }

    "throw exception when given v0.1.0.1 (a tag with an incorrect format)" in new TestSetupForRelease {
      intercept[IllegalArgumentException] {
        SbtGitVersioningForRelease.version("v0.1.0.1", 0)
      }.getMessage shouldBe "invalid version format for 'v0.1.0.1'"
    }

    "use the major version if provided" in new TestSetupForRelease {
      SbtGitVersioningForRelease.version("v0.1.0-1-g1234567", 1) shouldBe "1.0.0"
    }

    "use the major version and return 1.0.0 when HEAD has a tag already. Can only happen on local dev environments" in new TestSetupForRelease {
      SbtGitVersioningForRelease.version("v0.2.0", 1) shouldBe "1.0.0"
    }

  }

  trait TestSetupForSnapshot {
    val SbtGitVersioningForSnapshot = new SbtVersioning {
      // overriding the default MAKE_RELEASE as it is set in the CI, affecting the tests
      override val makeReleaseEnvName: String = "UNUSED"
    }
  }
  trait TestSetupForRelease {
    val SbtGitVersioningForRelease = new SbtVersioning {
      override val makeReleaseEnvName: String = "TEST_MAKE_RELEASE" // setup in build.sbt to provide env var for tests
    }
  }
}
