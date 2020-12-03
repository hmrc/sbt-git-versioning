# sbt-git-versioning

[ ![Download](https://api.bintray.com/packages/hmrc/sbt-plugin-releases/sbt-git-versioning/images/download.svg) ](https://bintray.com/hmrc/sbt-plugin-releases/sbt-git-versioning/_latestVersion) 

This plugin contains SBT settings to version an HMRC `sbt` project. 

It's sole purpose is to auto generate the correct value for the `version := ??` setting, based on the Git history.

It does this by using the [release-versioning](https://github.com/hmrc/release-versioning) library under the hood

## Usage

### Sbt 1.x
Since major version 2, this plugin is cross compiled for sbt 1.x (specifically 1.3.4).
| Sbt version | Plugin version |
| ----------- | -------------- |
| `<= 0.13.x` | `any`          |
| `>= 1.x`    | `>= 2.x`       |

addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "[INSERT_VERSION]")

## Controlling the version

By default, the version number starts at `0.1.0` and each new release will increment the `minor` segment. This can be
overridden by environment variables. 

So if the current version is `0.1.0`, the next version wil be:

DEFAULT => `0.2.0`
MAKE_RELEASE=true => `1.0.0`
MAKE_HOTFIX=true => `0.2.1`

You can see the version applied if you just run `sbt version` on your project. 

## Developing this plugin
This plugin uses a previous version of itself to set its sbt version. Be sure to update this if you need to make
changes to the plugin.

## License ##
 
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
