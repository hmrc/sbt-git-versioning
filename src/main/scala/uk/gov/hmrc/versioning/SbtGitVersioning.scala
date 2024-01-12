/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.versioning

import com.github.sbt.git.ConsoleGitRunner
import uk.gov.hmrc.versioning.ReleaseVersioning.calculateNextVersion
import sbt.Keys._
import sbt._

import scala.util.{Properties, Try}

object SbtGitVersioning extends sbt.AutoPlugin {

  override def trigger = allRequirements

  object autoImport {
    val majorVersion = settingKey[Int]("Sets the current major version")
  }

  import autoImport.majorVersion

  override def projectSettings = Seq(
    version := calculateNextVersion(
      release          = Properties.envOrNone("MAKE_RELEASE").exists(_.toBoolean),
      hotfix           = Properties.envOrNone("MAKE_HOTFIX").exists(_.toBoolean),
      releaseCandidate = Properties.envOrNone("MAKE_RELEASE_CANDIDATE").exists(_.toBoolean),
      maybeGitDescribe = runGitDescribe(baseDirectory.value, errorLogger(sLog.value)),
      majorVersion     = majorVersion.value
    )
  )

  private def runGitDescribe(baseDirectory: File, logger: Logger): Option[String] =
    Try {
      ConsoleGitRunner("describe", "--first-parent")(baseDirectory, logger)
    }.toOption

  private def errorLogger(logger: Logger): Logger =
    new Logger {
      def log(level: Level.Value, message: => String): Unit =
        if (level >= Level.Warn)
          logger.log(level, message)

      def success(message: => String): Unit =
        ()

      def trace(t: => Throwable): Unit =
        ()
    }
}
