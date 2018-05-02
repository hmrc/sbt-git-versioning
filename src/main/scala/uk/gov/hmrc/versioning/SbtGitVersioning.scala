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
import sbt.{ConsoleLogger, _}

import scala.util.Properties

object SbtGitVersioning extends SbtGitVersioning

trait SbtGitVersioning extends sbt.AutoPlugin {

  lazy val majorVersion = settingKey[Int]("Sets the current major version")

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

  def version(gitDescribe: String, majorVersion: Int): String = {

    val version: String = Properties.envOrNone(makeReleaseEnvName) match {
      case Some(_) => nextVersion(gitDescribe, majorVersion)
      case None    => nextVersion(gitDescribe, majorVersion) + "-SNAPSHOT"
    }

    logger.info(s"sbt git versioned as $version")
    version
  }

  private object AsInt {
    def unapply(arg: String): Option[Int] = Some(arg.toInt)
  }

  def nextVersion(gitDescribe: String, requestedMajorVersion: Int): String = {
    val gitDescribeFormat = """^(?:release\/|v)?(\d+)\.(\d+)\.(?:\d+)(?:-.*-g.*$){0,1}""".r

    def validMajorVersion(current: Int, requested: Int): Boolean =
      requested == current || requested == current + 1

    gitDescribe match {
      case gitDescribeFormat(AsInt(major), _) if !validMajorVersion(major, requestedMajorVersion) =>
        throw new IllegalArgumentException(
          s"Invalid majorVersion: $requestedMajorVersion. " +
            s"The accepted values are $major or ${major + 1} based on current git tags."
        )

      case gitDescribeFormat(AsInt(major), _) if requestedMajorVersion != major =>
        s"$requestedMajorVersion.0.0"

      case gitDescribeFormat(major, AsInt(minor)) =>
        s"$major.${minor + 1}.0"

      case unrecognizedGitDescribe =>
        throw new IllegalArgumentException(s"invalid version format for '$unrecognizedGitDescribe'")
    }
  }

}
