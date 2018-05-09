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
import com.typesafe.sbt.git.{ConsoleGitRunner, DefaultReadableGit}
import sbt.Keys.baseDirectory
import sbt.{ConsoleLogger, _}

import scala.util.Properties

object SbtGitVersioning extends SbtGitVersioning

trait SbtGitVersioning extends sbt.AutoPlugin {

  lazy val majorVersion = settingKey[Int]("Sets the current major version")

  val logger = ConsoleLogger()

  override def requires = GitVersioning

  override def trigger   = allRequirements
  val makeReleaseEnvName = "MAKE_RELEASE"
  val makeHotfixEnvName  = "MAKE_HOTFIX"

  override def projectSettings = Seq(
    git.useGitDescribe := true,
    git.versionProperty := "NONE",
    git.gitDescribedVersion := {
      // using local git instead of JGit which returned incorrect `describe`
      // when there are many tags attached to the same commit
      val gitDescribeFromNonJGit = ConsoleGitRunner("describe")(baseDirectory.value)
      Some(version(gitDescribeFromNonJGit, majorVersion.value))
    },
    git.gitCurrentTags := {
      // overriding default lexicographic order of tags and sorting tags
      // according to our versioning
      new DefaultReadableGit(baseDirectory.value)
        .withGit(_.currentTags)
        .sortWith(versionComparator)
    },
    git.gitTagToVersionNumber := (tag => Some(version(tag, majorVersion.value))),
    git.uncommittedSignifier := None
  )

  def versionComparator(tag1: String, tag2: String): Boolean = {
    val Version = """(?:release\/|v)(\d+)\.(\d+)\.(\d+)""".r
    (tag1, tag2) match {
      case (Version(AsInt(major1), _, _), Version(AsInt(major2), _, _)) if major1 != major2 => major1 < major2
      case (Version(_, AsInt(minor1), _), Version(_, AsInt(minor2), _)) if minor1 != minor2 => minor1 < minor2
      case (Version(_, _, AsInt(patch1)), Version(_, _, AsInt(patch2))) if patch1 != patch2 => patch1 < patch2
      case (Version(_, _, _), _)                                                            => false
      case (_, Version(_, _, _))                                                            => true
      case (_, _)                                                                           => true
    }
  }

  def version(tagOrGitDescribe: String, majorVersion: Int): String =
    Properties.envOrNone(makeReleaseEnvName) match {
      case Some(_) => nextVersion(tagOrGitDescribe, majorVersion)
      case None    => nextVersion(tagOrGitDescribe, majorVersion) + "-SNAPSHOT"
    }

  private object AsInt {
    def unapply(arg: String): Option[Int] = Some(arg.toInt)
  }

  def nextVersion(gitDescribe: String, requestedMajorVersion: Int): String = {
    val gitDescribeFormat = """^(?:release\/|v)?(\d+)\.(\d+)\.(\d+)(?:-.*-g.*$){0,1}""".r

    def validMajorVersion(current: Int, requested: Int): Boolean =
      requested == current || requested == current + 1

    val makeHotfix = Properties.envOrNone(makeHotfixEnvName).fold(false)(_.toBoolean)

    gitDescribe match {
      case gitDescribeFormat(AsInt(major), _, _) if major != requestedMajorVersion && makeHotfix =>
        throw new IllegalArgumentException(
          s"Invalid majorVersion: $requestedMajorVersion. $makeHotfixEnvName is also set to true. " +
            "It is not possible to change the major version as part of a hotfix."
        )
      case gitDescribeFormat(AsInt(major), _, _) if !validMajorVersion(major, requestedMajorVersion) =>
        throw new IllegalArgumentException(
          s"Invalid majorVersion: $requestedMajorVersion. " +
            s"The accepted values are $major or ${major + 1} based on current git tags."
        )

      case gitDescribeFormat(AsInt(major), _, _) if requestedMajorVersion != major =>
        s"$requestedMajorVersion.0.0"

      case gitDescribeFormat(major, minor, AsInt(patch)) if makeHotfix =>
        s"$major.$minor.${patch + 1}"

      case gitDescribeFormat(major, AsInt(minor), _) =>
        s"$major.${minor + 1}.0"

      case unrecognizedGitDescribe =>
        throw new IllegalArgumentException(s"invalid version format for '$unrecognizedGitDescribe'")
    }
  }

}
