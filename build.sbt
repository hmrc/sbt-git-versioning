lazy val project = Project("sbt-git-versioning", file("."))
  .settings(
    majorVersion := 2,
    isPublicArtefact := true,
    sbtPlugin := true,
    scalaVersion := "2.12.14",
    crossSbtVersions := Vector("0.13.18", "1.3.4"),
    resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns),
    libraryDependencies ++= Seq(
      "uk.gov.hmrc" %% "release-versioning" % "0.17.0"
    )
  )

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")
