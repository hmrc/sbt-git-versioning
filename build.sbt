
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
      gitDescribe => Some(version(gitDescribe, 1))
    },
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "org.pegdown"   % "pegdown"    % "1.5.0" % "test"
    ),
    fork in Test := true,
    envVars in Test := Map("TEST_MAKE_RELEASE" -> "true")
  )

/**
  * Copied from uk.gov.hmrc.versioning.SbtVersioning to allow
  * correct versioning of this plugin
  */
def version(gitDescribe: String, majorVersion: Int): String = {

  val version: String = Properties.envOrNone("MAKE_RELEASE") match {
    case Some(_) => nextVersion(gitDescribe, majorVersion)
    case None    => nextVersion(gitDescribe, majorVersion) + "-SNAPSHOT"
  }

  logger.info(s"sbt git versioned as $version")
  version
}

val gitDescribeFormat = """^(?:release\/|v)?(\d+)\.(\d+)\.(\d+)(?:-.*-g.*$){0,1}""".r

def nextVersion(gitDescribe: String, majorVersion: Int): String =
  gitDescribe match {
    case gitDescribeFormat(major, _, _) if majorVersion != major.toInt =>
      s"$majorVersion.0.0"

    case gitDescribeFormat(major, minor, patch) =>
      s"$major.${minor.toInt + 1}.$patch"

    case unrecognizedGitDescribe =>
      throw new IllegalArgumentException(s"invalid version format for '$unrecognizedGitDescribe'")
  }


