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
import com.typesafe.sbt.git.{DefaultReadableGit, JGit}
import sbt.Keys.baseDirectory
import sbt._
import scala.util.Properties

object SbtGitVersioning extends sbt.AutoPlugin {

  override def requires = GitVersioning

  override def trigger = allRequirements

  object autoImport {
    val majorVersion = settingKey[Int]("Sets the current major version")
  }

  import ReleaseVersioning._
  import autoImport.majorVersion

  override def projectSettings = Seq(
    git.useGitDescribe := true,
    git.versionProperty := "NONE",
    git.gitDescribedVersion := Some(
      version(
        release      = Properties.envOrNone("MAKE_RELEASE").exists(_.toBoolean),
        hotfix       = Properties.envOrNone("MAKE_HOTFIX").exists(_.toBoolean),
        latestTag    = getAllTags(baseDirectory.value).reverse.headOption,
        majorVersion = majorVersion.value
      )),
    git.gitCurrentTags := getCurrentTags(baseDirectory.value),
    git.gitTagToVersionNumber := { tag =>
      Some(
        version(
          release      = Properties.envOrNone("MAKE_RELEASE").exists(_.toBoolean),
          hotfix       = Properties.envOrNone("MAKE_HOTFIX").exists(_.toBoolean),
          latestTag    = Some(tag),
          majorVersion = majorVersion.value
        ))
    },
    git.uncommittedSignifier := None
  )

  private def getCurrentTags(repo: File): Seq[String] =
    // overriding default lexicographic order of tags and sorting tags
    // according to our versioning
    new DefaultReadableGit(repo)
      .withGit(_.currentTags)
      .sortWith(versionComparator)

  private def getAllTags(repo: File): Seq[String] =
    // overriding default lexicographic order of tags and sorting tags
    // according to our versioning
    JGit(repo).tags
      .map(_.getName.replace("refs/tags/", ""))
      .sortWith(versionComparator)

  def versionComparator(tag1: String, tag2: String): Boolean = {
    val Version = """(?:release\/|v)?(\d+)\.(\d+)\.(\d+)""".r
    (tag1, tag2) match {
      case (Version(AsInt(major1), _, _), Version(AsInt(major2), _, _)) if major1 != major2 => major1 < major2
      case (Version(_, AsInt(minor1), _), Version(_, AsInt(minor2), _)) if minor1 != minor2 => minor1 < minor2
      case (Version(_, _, AsInt(patch1)), Version(_, _, AsInt(patch2))) if patch1 != patch2 => patch1 < patch2
      case (Version(_, _, _), _)                                                            => false
      case (_, Version(_, _, _))                                                            => true
      case (_, _)                                                                           => true
    }
  }

  private object AsInt {
    def unapply(arg: String): Option[Int] = Some(arg.toInt)
  }
}
