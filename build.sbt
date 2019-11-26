import sbt.Keys._
import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

val logger     = ConsoleLogger()
val pluginName = "sbt-git-versioning"

lazy val project = Project(pluginName, file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(
    majorVersion := 1,
    makePublicallyAvailableOnBintray := true,
    sbtPlugin := true,
    scalaVersion := "2.11.8",
    crossSbtVersions := Vector("0.13.18", "1.3.4"),
    targetJvm := "jvm-1.7",
    resolvers += Resolver.url(
      "bintray-sbt-plugin-releases",
      url("https://dl.bintray.com/content/sbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns),
    libraryDependencies ++= Seq(
      "uk.gov.hmrc"   %% "release-versioning" % "0.16.0"
    )
  )

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")
