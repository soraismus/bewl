package com.fdilke.bewl.algebra

import com.fdilke.bewl.fsets.FiniteSets
import com.fdilke.bewl.fsets.FiniteSetsUtilities._
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class AlgebraicStructuresTest extends FunSpec {

  private val topos = com.fdilke.bewl.fsets.FiniteSets
  import topos._

  private val (i, x, y, a, b, c, d, e, f, f2, g, g2, r, s) =
    ('i,'x,'y,'a,'b,'c,'d,'e,'f,'f2,'g,'g2, 'r, 's)

  describe("Monoids") {
    it("can be constructed and verified") {
      val carrier = dot(i, x, y)
      val unit = makeNullaryOperator(carrier, i)
      val product = makeBinaryOperator(carrier,
        (i, i) -> i, (i, x) -> x, (i, y) -> y,
        (x, i) -> x, (x, x) -> x, (x, y) -> x,
        (y, i) -> y, (y, x) -> y, (y, y) -> y
      )
      Monoid(carrier, unit, product).sanityTest
    }

    it("enforce the left unit element") {
      intercept[IllegalArgumentException] {
        monoidFromTable(
          i, i, i,
          x, x, x,
          y, y, y
        ).sanityTest
      }.getMessage shouldBe "left unit law failed"
    }

    it("enforce the right unit law") {
      intercept[IllegalArgumentException] {
        monoidFromTable(
          i, x, y,
          i, x, y,
          i, x, y
        ).sanityTest
      }.getMessage shouldBe "right unit law failed"
    }

    it("enforce associative multiplication") {
      intercept[IllegalArgumentException] {
        monoidFromTable(
          i, x, y,
          x, y, y,
          y, x, y
        ).sanityTest
      }.getMessage shouldBe "associative law failed"
    }

    it("can validate the triadic monoid") {
      monoidFromTable(
        i, a, b, c, f,f2, g,g2,
        a, a, a, a, a, a, a, a,
        b, b, b, b, b, b, b, b,
        c, c, c, c, c, c, c, c,
        f, b, c, b, f2,f, b, b,
        f2,c, b, c, f,f2, c, c,
        g, c, a, a, a, a,g2, g,
        g2,a, c, c, c, c, g,g2
      ).sanityTest
    }

    it("can tell if a monoid is commutative") {
      monoidFromTable(
        i, a, b,
        a, a, b,
        b, b, b
      ) should be('commutative)
      monoidFromTable(
        i, a, b,
        a, a, a,
        b, b, b
      ) should not be('commutative)
    }
  }

  val monoid4 =
    monoidFromTable(
      i, x, y,
      x, x, x,
      y, y, y
    )

  describe("Monoid actions") {
    it("can be constructed and validated") {
      monoid4.action(dot(a, b))(Function untupled Map(
        (a, i) -> a, (a, x) -> a, (a, y) -> a,
        (b, i) -> b, (b, x) -> b, (b, y) -> b
      )).sanityTest
    }

    it("enforce the right unit law") {
      intercept[IllegalArgumentException] {
        monoid4.action(dot(a, b))(Function untupled Map(
          (a, i) -> b, (a, x) -> a, (a, y) -> a,
          (b, i) -> a, (b, x) -> b, (b, y) -> b
        )).sanityTest
      }.getMessage shouldBe "right unit law failed"
    }

    ignore("enforce the associative law") {
      intercept[IllegalArgumentException] {
        monoid4.action(dot(a, b))(Function untupled Map(
          (a, i) -> a, (a, x) -> b, (a, y) -> a,
          (b, i) -> b, (b, x) -> b, (b, y) -> a
        )).sanityTest
      }.getMessage shouldBe "Associative law for monoid action *"
    }
  }

  describe("Groups") {
    it("can be defined with an appropriate unit, multiplication and inverse") {
      val carrier = dot(i, x, y)
      val unit = makeNullaryOperator(carrier, i)
      val inverse = makeUnaryOperator(carrier,
        i -> i, x -> y, y -> x
      )
      val product = makeBinaryOperator(carrier,
        (i, i) -> i, (i, x) -> x, (i, y) -> y,
        (x, i) -> x, (x, x) -> y, (x, y) -> i,
        (y, i) -> y, (y, x) -> i, (y, y) -> x
      )
      Group(carrier, unit, product, inverse).sanityTest
    }

    it("must have inverses for every element") {
      val carrier = dot(i, x, y)
      val unit = makeNullaryOperator(carrier, i)
      val inverse = makeUnaryOperator(carrier,
        i -> i, x -> y, y -> x
      )
      val product = makeBinaryOperator(carrier,
        (i, i) -> i, (i, x) -> x, (i, y) -> y,
        (x, i) -> x, (x, x) -> x, (x, y) -> x,
        (y, i) -> y, (y, x) -> y, (y, y) -> y
      )
      intercept[IllegalArgumentException] {
        Group(carrier, unit, product, inverse).sanityTest
      }.getMessage shouldBe "left inverse law failed"
    }

    it("can tell if a group is commutative") {
      val carrier = dot(i, x, y)
      val unit = makeNullaryOperator(carrier, i)
      val inverse = makeUnaryOperator(carrier,
        i -> i, x -> y, y -> x
      )
      val product = makeBinaryOperator(carrier,
        (i, i) -> i, (i, x) -> x, (i, y) -> y,
        (x, i) -> x, (x, x) -> y, (x, y) -> i,
        (y, i) -> y, (y, x) -> i, (y, y) -> x
      )
      val group = Group(carrier, unit, product, inverse)
      group shouldBe 'commutative
    }

    it("can tell if a group is not commutative") {
      val group = groupOfUnits(monoidFromTable(
        i, a, b, c, r, s,
        a, i, s, r, c, b,
        b, r, i, s, a, c,
        c, s, r, i, b, a,
        r, b, c, a, s, i,
        s, c, a, b, i, r
      ))._1
      group.sanityTest
      group.carrier.globals.size shouldBe 6
      group should not be('commutative)
    }

    it("can be regarded as monoids") {
      val largerMonoid = endomorphismMonoid(dot(1, 2, 3))
      val (group, inject) = groupOfUnits(largerMonoid)
      val monoid = group.asMonoid
      monoid.sanityTest
      monoids.isMorphism(monoid, largerMonoid, inject)
    }
  }
}
