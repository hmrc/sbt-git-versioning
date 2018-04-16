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

import com.typesafe.sbt.GitVersioning
import com.typesafe.sbt.SbtGit.git
import sbt.ConsoleLogger

import scala.util.Properties

object SbtGitVersioning extends SbtVersioning

trait SbtVersioning extends sbt.AutoPlugin {

  val logger = ConsoleLogger()

  override def requires = GitVersioning

  override def trigger   = allRequirements
  val makeReleaseEnvName = "MAKE_RELEASE"

  override def projectSettings = Seq(
    git.useGitDescribe := true,
    git.versionProperty := "NONE",
    git.gitTagToVersionNumber := { tag =>
      Some(version(tag))
    },
    git.uncommittedSignifier := None
  )

  def version(tag: String): String = {
    val version: String = Properties.envOrNone(makeReleaseEnvName) match {
      case Some(_) => makeARelease(tag)
      case None    => makeASnapshot(tag)
    }

    logger.info(s"sbt git versioned as $version")
    version
  }

  private val snapshotFormat = """^(?:release\/|v)?(\d+)\.(\d+)\.(\d+)-(.*-g.*$)""".r
  private val releaseFormat  = """^(?:release\/|v)?(\d+)\.(\d+)\.(\d+)$""".r

  private val makeARelease: String => String = {
    case releaseFormat(major, minor, patch) => s"$major.$minor.$patch"
    case snapshotFormat(major, minor, _, _) => s"$major.${Integer.valueOf(minor) + 1}.0"
    case tag                                => throw new IllegalArgumentException(s"invalid version format for '$tag'")
  }

  private val makeASnapshot: String => String = {
    case snapshotFormat(major, minor, patch, sha) => s"$major.$minor.$patch-$sha"
    case releaseFormat(major, minor, patch)       => s"$major.$minor.$patch-0-g0000000"
    case tag                                      => throw new IllegalArgumentException(s"invalid version format for '$tag'")
  }
}
