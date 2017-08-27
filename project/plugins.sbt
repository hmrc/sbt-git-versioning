resolvers += Resolver.url("hmrc-sbt-plugin-releases",
  url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
  
resolvers += Resolver.bintrayRepo("hmrc", "releases")  

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "1.6.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")



