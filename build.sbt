lazy val project = Project("sbt-git-versioning", file("."))
  .settings(
    majorVersion     := 2,
    isPublicArtefact := true,
    sbtPlugin        := true,
    scalaVersion     := "2.12.18",
    resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns),
    libraryDependencies ++= Seq(
      "uk.gov.hmrc" %% "release-versioning" % "0.18.0"
    )
  )

addSbtPlugin("com.github.sbt" % "sbt-git" % "2.0.1")
