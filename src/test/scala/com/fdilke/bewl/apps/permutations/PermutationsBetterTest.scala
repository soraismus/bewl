package com.fdilke.bewl.apps.permutations

import com.fdilke.bewl.apps.permutations.Parity.{EVEN, ODD}
import com.fdilke.bewl.apps.permutations.Permutations.topos.{DOT, WRAPPER}
import com.fdilke.bewl.apps.permutations.Permutations.{RichPermutation, π}
import com.fdilke.bewl.fsets.FiniteSets
import org.scalatest.FunSpec
import org.scalatest.Matchers._

import scala.language.postfixOps

class PermutationsBetterTest extends FunSpec {

  describe("Permutations") {
    it("can be defined, unwrapped, and examined as maps") {
      val perm: DOT[WRAPPER[Int]] = π(1,2)(3)π

      perm.sanityTest

      val expectedCarrier = FiniteSets.makeDot(Set(1, 2, 3))

      perm.asArrow shouldBe expectedCarrier(expectedCarrier) {
        Map(
          1 -> 2, 2 -> 1, 3 -> 3
        )
      }

      perm.asMap shouldBe Map(
        1 -> 2, 2 -> 1, 3 -> 3
      )
    }

    it("can be tested on their parity") {
      (π π).parity shouldBe EVEN
      (π(1,2)π).parity shouldBe ODD
      (π(1,2,3)π).parity shouldBe EVEN
      (π(1,2)(3,4)π).parity shouldBe EVEN
      (π(1,2,3,4)π).parity shouldBe ODD
    }

    it("come with extra syntactic sugar") {
      val permutation = π(1, 2)π

      permutation.carrier shouldBe Set(1, 2)

      permutation.send(1) shouldBe 2
      permutation.send(2) shouldBe 1

      intercept[NoSuchElementException] {
        permutation.send(3)
      }
    }

    it("can be composed when they're compatible") {
      intercept[IllegalArgumentException] {
        (π(1,2)π) * (π(2,3)π)
      }
      (π(1,2)(3)π) * (π(1)(2,3)π) shouldBe (π(1,3,2)π)
    }
  }
}