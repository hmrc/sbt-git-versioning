
import com.typesafe.sbt.GitVersioning
import com.typesafe.sbt.SbtGit.git
import sbt.Keys._
import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.SbtAutoBuildPlugin

import scala.util.Properties

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
    git.gitTagToVersionNumber := {
      tag => Some(version(tag))
    },
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "org.pegdown"   % "pegdown"    % "1.5.0" % "test"
    ),
    fork in Test := true,
    envVars in Test := Map("TEST_MAKE_RELEASE" -> "true")
  )

def version(tag: String): String = {
  val version: String = Properties.envOrNone("MAKE_RELEASE") match {
    case Some(_) => makeARelease(tag)
    case None    => makeASnapshot(tag)
  }

  logger.info(s"sbt git versioned as $version")
  version
}

val snapshotFormat = """^(?:release\/|v)?(\d+)\.(\d+)\.(\d+)-(.*-g.*$)""".r
val releaseFormat  = """^(?:release\/|v)?(\d+)\.(\d+)\.(\d+)$""".r

val makeARelease: String => String = {
  case releaseFormat(major, minor, patch) => s"$major.$minor.$patch"
  case snapshotFormat(major, minor, _, _) => s"$major.${Integer.valueOf(minor) + 1}.0"
  case tag                                => throw new IllegalArgumentException(s"invalid version format for '$tag'")
}

val makeASnapshot: String => String = {
  case snapshotFormat(major, minor, patch, sha) => s"$major.$minor.$patch-$sha"
  case releaseFormat(major, minor, patch)       => s"$major.$minor.$patch-0-g0000000"
  case tag                                      => throw new IllegalArgumentException(s"invalid version format for '$tag'")
}
