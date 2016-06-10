package com.fdilke.bewl.topos.structures

import com.fdilke.bewl.topos.BaseTopos
import org.scalatest.Matchers._

import scala.language.{higherKinds, postfixOps, reflectiveCalls}

trait Monads {
  Ɛ: BaseTopos =>

  trait Monad[
    M[X <: ~] <: ~
  ] {
    def apply[
      X <: ~
    ] (
      dot: DOT[X]
    ): At[X]

    def map[
      X <: ~,
      Y <: ~
    ] (
      arrow: X > Y
    ): M[X] > M[Y]

    trait At[
      X <: ~
    ] {
      val free: DOT[M[X]]
      val eta: X > M[X]
      val mu: M[M[X]] > M[X]
    }

    case class Algebra[
      X <: ~
    ](
      structure: M[X] > X
    ) {
      lazy val carrier = structure target
      lazy val local = apply(carrier)

      def sanityTest() =
        (structure o local.eta) shouldBe carrier.identity
      def sanityTest2() =
        (structure o map(structure)) shouldBe (structure o local.mu)
    }
  }

  object Monad {
    def sanityTest[
      M[X <: ~] <: ~,
      X <: ~
    ](
      monad: Monad[M],
      dot: DOT[X]
    ) = {
      val at = monad(dot)
      import at._

      mu o monad.map(eta) shouldBe free.identity
      mu o monad(free).eta shouldBe free.identity
    }

    def sanityTest2[
      M[X <: ~] <: ~,
      X <: ~
    ](
      monad: Monad[M],
      dot: DOT[X]
    ) = {

      val at = monad(dot)
      import at._

      mu o monad.map(mu) shouldBe (mu o monad(free).mu)
    }
  }
}


