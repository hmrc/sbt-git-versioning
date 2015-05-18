/*
 * Copyright 2015 HM Revenue & Customs
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

object SbtGitVersioning extends sbt.AutoPlugin {

  val logger = ConsoleLogger()

  override def requires = GitVersioning

  override def trigger = allRequirements

  override def projectSettings = Seq (
    git.useGitDescribe := true,
    git.versionProperty := "NONE",
    git.gitTagToVersionNumber := { tag => Some(updateTag(tag)) },
    git.gitDescribedVersion <<= {
      git.gitDescribedVersion((vO) => {
        val deNulledVersion: Option[String] = vO.flatMap{ vOO => Option(vOO) }
        deNulledVersion map updateTag
      })
    }
  )

  def updateTag(tag:String):String={
    val removedV = if (tag.startsWith("v")) tag.drop(1) else tag

    val gitDescribeFormat = """^(\d+\.)?(\d+\.)?(\d+)?.*-.*-g.*$"""
    val standardFormat    = """^(\d+\.)?(\d+\.)?(\d+)?$"""

    removedV.matches(gitDescribeFormat) match {
      case true  => removedV
      case false =>
        removedV.matches(standardFormat) match {
        case true => removedV + "-0-g0000000"
        case false => throw new IllegalArgumentException(s"invalid version format for '$tag'")
      }
    }
  }
}
