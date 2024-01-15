# sbt-git-versioning

This plugin contains SBT settings to version an HMRC `sbt` project.

It's sole purpose is to auto generate the correct value for the `version := ??` setting, based on the Git history.

It does this by using the [release-versioning](https://github.com/hmrc/release-versioning) library under the hood

## Usage

In your project/plugins.sbt file:
```scala
resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "[INSERT_VERSION]")
```

The library has supported sbt 1.x since version `2.0.0` and has dropped support for sbt 0.13 since `2.5.0`

## Controlling the version

By default, the version number starts at `0.1.0` and each new release will increment the `minor` segment. This can be
overridden by environment variables.

So if the current version is `0.1.0`, the next version wil be:

DEFAULT => `0.2.0-SNAPSHOT`
MAKE_RELEASE=true => `0.2.0`
MAKE_HOTFIX=true => `0.2.1`

To change the major version, you can override `majorVersion` in build.sbt.

```scala
ThisBuild / majorVersion := 1
```

You can see the version applied if you can run `sbt version` on your project.

## Developing this plugin
This plugin uses a previous version of itself to set its sbt version. Be sure to update this if you need to make
changes to the plugin.

## License ##

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
