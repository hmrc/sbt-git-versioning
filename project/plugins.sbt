resolvers += Resolver.url("hmrc-sbt-plugin-releases",
  url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
  
resolvers += Resolver.bintrayRepo("hmrc", "releases")  

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "0.5.0-SNAPSHOT")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.4")



