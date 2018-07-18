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

import scala.util.Random

class SbtVersioningSpec extends WordSpec with Matchers with TryValues with OptionValues {

  "nextVersion" should {
    "return 0.2.0-SNAPSHOT when git describe is v0.1.1-1-g1234567" in {
      SbtGitVersioning.nextVersion("v0.1.1-1-g1234567", None, 0) shouldBe "0.2.0"
    }

    "return 0.2.0 when git describe is v0.1.0 (a tag on HEAD)" in {
      SbtGitVersioning.nextVersion("v0.1.0", None, 0) shouldBe "0.2.0"
    }

    "return 0.1.0 when git describe returns the id on HEAD (no tags)" in {
      SbtGitVersioning.nextVersion("12345", Some("123456789"), 0) shouldBe "0.1.0"
    }

    "throw exception when given v0.1.0.1 (a tag with an incorrect format)" in {
      intercept[IllegalArgumentException] {
        SbtGitVersioning.nextVersion("v0.1.0.1", None, 0)
      }.getMessage shouldBe "invalid version format for 'v0.1.0.1'"
    }

    "use the new major version" in {
      SbtGitVersioning.nextVersion("v0.1.0-1-g1234567", None, 1) shouldBe "1.0.0"
    }

    "use the new major version and return 1.0.0 when given v0.1.0 (a tag on HEAD)" in {
      SbtGitVersioning.nextVersion("v0.1.0", None, 1) shouldBe "1.0.0"
    }

    "create a new patch if MAKE_HOTFIX is true" in {
      val makeHotfixEnvVar = "TEST_MAKE_HOTFIX" // made available in build.sbt
      val sbtGitVersioning = new VersioningFromGitDescribe {
        override val makeHotfixEnvName: String = makeHotfixEnvVar
      }
      sbtGitVersioning.nextVersion("v0.1.0", None, 0) shouldBe "0.1.1"
    }

    "throw an exception if a new major is requested at the same time as a hotfix" in {
      val makeHotfixEnvVar = "TEST_MAKE_HOTFIX" // made available in build.sbt
      val sbtGitVersioning = new VersioningFromGitDescribe {
        override val makeHotfixEnvName: String = makeHotfixEnvVar
      }
      intercept[IllegalArgumentException] {
        sbtGitVersioning.nextVersion("v0.1.0", None, 1)
      }.getMessage shouldBe "Invalid majorVersion: 1. TEST_MAKE_HOTFIX is also set to true. " +
        "It is not possible to change the major version as part of a hotfix."
    }

    "throw exception if new major version is > current version + 1" in {
      intercept[IllegalArgumentException] {
        SbtGitVersioning.nextVersion("v0.1.0", None, 2)
      }.getMessage shouldBe "Invalid majorVersion: 2. The accepted values are 0 or 1 based on current git tags."
    }

    "throw exception if new major version is < current version" in {
      intercept[IllegalArgumentException] {
        SbtGitVersioning.nextVersion("v1.1.0", None, 0)
      }.getMessage shouldBe "Invalid majorVersion: 0. The accepted values are 1 or 2 based on current git tags."
    }
  }

  "version" should {

    /*
     * Plugin will return either a snapshot version or release version.
     * This is decided based on whether an environment variable MAKE_RELEASE is set.
     * Since CI will have this variable set by default this test
     * overrides the expected environment variable name to TEST_MAKE_RELEASE
     * which is made available in build.sbt or DUMMY_ENV_VAR which is unset
     * to allow unit testing.
     */

    "return next version with -SNAPSHOT suffix if MAKE_RELEASE env variable not set" in {
      val makeReleaseEnvVar = "DUMMY_ENV_VAR"
      val sbtGitVersioning = new VersioningFromGitDescribe {
        override val makeReleaseEnvName: String = makeReleaseEnvVar
      }

      sbtGitVersioning.version("v1.1.0", None, 1) shouldBe "1.2.0-SNAPSHOT"
    }

    "return next version if MAKE_RELEASE env var is set" in {
      val makeReleaseEnvVar = "TEST_MAKE_RELEASE" // made available in build.sbt
      val sbtGitVersioning = new VersioningFromGitDescribe {
        override val makeReleaseEnvName: String = makeReleaseEnvVar
      }

      sbtGitVersioning.version("v1.1.0", None, 1) shouldBe "1.2.0"
    }
  }

  "sorting tags" should {
    "consider major versions first" in {
      assertOrder(List("v8.0.0", "v9.0.0", "v10.10.0"))
    }
    "consider minor versions second" in {
      assertOrder(List("v0.8.0", "v0.9.0", "v0.10.0"))
    }
    "consider hotfix versions third" in {
      assertOrder(List("v0.1.8", "v0.1.9", "v0.1.10"))
    }
    "deprioritize tags in unknown formats" in {
      assertOrder(List("not-a-valid-tag", "not-a-valid-tag", "v0.1.0", "v1.0.0"))
    }
    "work for both v and release/ styles" in {
      assertOrder(List("release/1.0.0", "release/1.1.0", "v2.0.0"))
    }
  }

  def assertOrder(expectedOrder: List[String]): Unit =
    (1 to 25).foreach { _ =>
      val unsorted = Random.shuffle(expectedOrder)
      unsorted.sortWith(SbtGitVersioning.versionComparator) shouldBe expectedOrder
    }

}
