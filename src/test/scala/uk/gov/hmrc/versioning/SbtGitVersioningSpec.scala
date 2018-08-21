package uk.gov.hmrc.versioning
import org.scalatest.{Matchers, WordSpec}
import scala.util.Random

class SbtGitVersioningSpec extends WordSpec with Matchers {
  "sorting tags" should {
    "consider major versions first" in {
      assertOrder(List("v8.0.0", "v9.0.0", "v10.10.0"))
    }
    "consider minor versions second" in {
      assertOrder(List("v0.8.0", "v0.9.0", "v0.10.0"))
    }
    "consider hotfix versions third" in {
      assertOrder(List("v0.1.8", "v0.1.9", "v0.1.10"))
    }
    "deprioritize tags in unknown formats" in {
      assertOrder(List("not-a-valid-tag", "not-a-valid-tag", "v0.1.0", "v1.0.0"))
    }
    "work for both v and release/ styles" in {
      assertOrder(List("release/1.0.0", "release/1.1.0", "v2.0.0"))
    }
  }

  def assertOrder(expectedOrder: List[String]): Unit =
    (1 to 25).foreach { _ =>
      val unsorted = Random.shuffle(expectedOrder)
      unsorted.sortWith(SbtGitVersioning.versionComparator) shouldBe expectedOrder
    }

}
