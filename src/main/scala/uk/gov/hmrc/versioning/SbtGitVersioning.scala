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
import sbt.{ConsoleLogger, Def, _}

import scala.util.Properties

object SbtGitVersioning extends SbtVersioning

trait SbtVersioning extends sbt.AutoPlugin {

  lazy val majorVersion = settingKey[Option[Int]]("Sets the current major version")

  override def globalSettings: Seq[Def.Setting[_]] = Seq(
    majorVersion := None
  )

  val logger = ConsoleLogger()

  override def requires = GitVersioning

  override def trigger   = allRequirements
  val makeReleaseEnvName = "MAKE_RELEASE"

  override def projectSettings = Seq(
    git.useGitDescribe := true,
    git.versionProperty := "NONE",
    git.gitTagToVersionNumber := (tag => Some(version(tag, majorVersion.value))),
    git.uncommittedSignifier := None
  )

  def version(gitDescribe: String, majorVersion: Option[Int]): String = {

    val version: String = Properties.envOrNone(makeReleaseEnvName) match {
      case Some(_) => nextVersion(gitDescribe, majorVersion)
      case None    => nextVersion(gitDescribe, majorVersion) + "-SNAPSHOT"
    }

    logger.info(s"sbt git versioned as $version")
    version
  }

  private val gitDescribeFormat = """^(?:release\/|v)?(\d+)\.(\d+)\.(\d+)(?:-.*-g.*$){0,1}""".r

  private def nextVersion(gitDescribe: String, majorVersion: Option[Int]): String =
    (gitDescribe, majorVersion) match {
      case (gitDescribeFormat(major, _, _), Some(newMajor)) if newMajor != major.toInt =>
        s"$newMajor.0.0"

      case (gitDescribeFormat(major, minor, patch), _) =>
        s"$major.${minor.toInt + 1}.$patch"

      case (unrecognizedGitDescribe, _) =>
        throw new IllegalArgumentException(s"invalid version format for '$unrecognizedGitDescribe'")
    }

}
