import com.typesafe.sbt.SbtGit.git
import uk.gov.hmrc.DefaultBuildSettings.targetJvm

enablePlugins(SbtAutoBuildPlugin, GitVersioning)

name := "sbt-git-versioning"

val logger = ConsoleLogger()

sbtPlugin := true
scalaVersion := "2.10.5"
targetJvm := "jvm-1.7"

resolvers := Seq(
  Resolver.bintrayRepo("hmrc", "releases"),
  Resolver.typesafeRepo("releases"),
  Resolver.url("bintray-sbt-plugin-releases",
    url("https://dl.bintray.com/content/sbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
)

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.3" % "test",
  "org.pegdown" % "pegdown" % "1.6.0" % "test"
)

BuildDescriptionSettings()

git.useGitDescribe := true
git.versionProperty := "NONE"
git.uncommittedSignifier := None
git.gitTagToVersionNumber := { tag =>
  val version = updateTag(tag)
  logger.info(s"sbt git versioned as $version")
  Some(version)
}
git.gitDescribedVersion := {
  git.gitDescribedVersion((vO) => {
    val deNulledVersion: Option[String] = vO.flatMap{ vOO => Option(vOO) }
    deNulledVersion map updateTag
  }).value
}


//This code is copied in [[uk.gov.hmrc.versioning.SbtGitVersioning#updateTag]]
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