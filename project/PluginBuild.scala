/*
 * Copyright 2015 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.typesafe.sbt.GitVersioning
import com.typesafe.sbt.SbtGit.git
import sbt.Keys._
import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.SbtAutoBuildPlugin

object PluginBuild extends Build {
  val logger     = ConsoleLogger()
  val pluginName = "sbt-git-versioning"

  lazy val project = Project(pluginName, file("."))
    .enablePlugins(SbtAutoBuildPlugin, GitVersioning)
    .settings(
      sbtPlugin := true,
      scalaVersion := "2.10.5",
      targetJvm := "jvm-1.7",
      resolvers += Resolver.url(
        "bintray-sbt-plugin-releases",
        url("https://dl.bintray.com/content/sbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns),
      addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5"),
      git.useGitDescribe := true,
      git.versionProperty := "NONE",
      git.uncommittedSignifier := None,
      git.gitTagToVersionNumber := { tag =>
        val version = updateTag(tag)
        logger.info(s"sbt git versioned as $version")
        Some(version)
      },
      git.gitDescribedVersion <<= {
        git.gitDescribedVersion((vO) => {
          val deNulledVersion: Option[String] = vO.flatMap { vOO =>
            Option(vOO)
          }
          deNulledVersion map updateTag
        })
      },
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "2.2.4" % "test",
        "org.pegdown"   % "pegdown"    % "1.5.0" % "test"
      ),
      fork in Test := true,
      envVars in Test := Map("TEST_MAKE_RELEASE" -> "true")
    )

  /**
    * This code is copied in [[uk.gov.hmrc.versioning.SbtGitVersioning#updateTag]], that
    * version is tested and should be copied here. I can't find an easy way to share it :(
    */
  def updateTag(tag: String): String = {
    val removedV = if (tag.startsWith("v")) tag.drop(1) else tag

    val gitDescribeFormat = """^(\d+\.)?(\d+\.)?(\d+)?.*-.*-g.*$"""
    val standardFormat    = """^(\d+\.)?(\d+\.)?(\d+)?$"""

    removedV.matches(gitDescribeFormat) match {
      case true => removedV
      case false =>
        removedV.matches(standardFormat) match {
          case true  => removedV + "-0-g0000000"
          case false => throw new IllegalArgumentException(s"invalid version format for '$tag'")
        }
    }
  }
}
