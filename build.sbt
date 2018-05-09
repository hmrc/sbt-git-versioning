
import com.typesafe.sbt.GitVersioning
import sbt.Keys._
import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.versioning.SbtGitVersioning.majorVersion

val logger     = ConsoleLogger()
val pluginName = "sbt-git-versioning"

lazy val project = Project(pluginName, file("."))
  .enablePlugins(SbtAutoBuildPlugin, GitVersioning, SbtGitVersioning)
  .settings(
    majorVersion  := 1,
    sbtPlugin     := true,
    scalaVersion  := "2.10.5",
    targetJvm     := "jvm-1.7",
    resolvers     += Resolver.url(
      "bintray-sbt-plugin-releases",
      url("https://dl.bintray.com/content/sbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "org.pegdown"   % "pegdown"    % "1.5.0" % "test"
    ),
    fork in Test := true,
    envVars in Test := Map("TEST_MAKE_RELEASE" -> "true", "TEST_MAKE_HOTFIX" -> "true")
  )

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")
