package com.fdilke.bewl.topos

import com.fdilke.bewl.helper.⊕
import com.fdilke.bewl.topos.constructions.ConstructToposOfMonoidActions
import org.scalatest.Matchers._
import org.scalatest._

abstract class ToposFixtureSanityTests[~, T <: Topos[~]](fixtures: ToposWithFixtures[~]) extends FunSpec {
  import fixtures._

  describe(s"The fixtures for ${fixtures.topos.getClass.getName}") {
    it("include distinct sane objects") {
      val objects = Set(foo, bar, baz)

      objects should have size 3

      objects foreach { _.sanityTest() }
    }

    it("include sane arrows whose sources and targets match their names") {
      foo2bar.sanityTest()
      foo2bar.source shouldBe foo
      foo2bar.target shouldBe bar

      foo2baz.sanityTest()
      foo2baz.source shouldBe foo
      foo2baz.target shouldBe baz

      foobar2baz.arrow.sanityTest()
      foobar2baz.product.left shouldBe foo
      foobar2baz.product.right shouldBe bar
      foobar2baz.arrow.source shouldBe (foo x bar)
      foobar2baz.arrow.target shouldBe baz

      monicBar2baz.sanityTest()
      monicBar2baz.source shouldBe bar
      monicBar2baz.target shouldBe baz

      equalizerSituation.sanityTest()
    }
  }
}

abstract class ToposWithFixtures[~] {
  val topos : Topos[~]

  type FOO <: ~
  type BAR <: ~
  type BAZ <: ~
  type SUB_BAR <: ~

  import topos.{~ => _, _}

  def makeSampleDot(): DOT[_ <: ~]
  def makeSampleArrow(): >[_ <: ~, _ <: ~]

  val foo : DOT[FOO]
  val bar : DOT[BAR]
  val baz : DOT[BAZ]

  val foo2bar : FOO > BAR
  val foo2ImageOfBar : FOO > BAZ
  val foobar2baz : BiArrow[FOO, BAR, BAZ]
  val monicBar2baz: BAR > BAZ

  val equalizerSituation: EqualizerSituation[_ <: ~, _ <: ~, _ <: ~]

  case class EqualizerSituation[S <: ~, M <: ~, T <: ~](
    r: S > M,
    s: M > T,
    t: M > T) {

    def sanityTest() {
      r.sanityTest()
      s.sanityTest()
      t.sanityTest()
    }

    if (s == t) {
      throw new IllegalArgumentException("equalizing two arrows that are already equal!")
    }

    (s o r) shouldBe (t o r)
  }

  final lazy val foo2baz = foo2ImageOfBar // a convenient alias
}

abstract class GenericToposTests[~](
  val fixtures: ToposWithFixtures[~]
) extends ToposFixtureSanityTests(fixtures) {

  import fixtures._
  import fixtures.topos.{~ => ~~, _}

  private lazy val inActionTopos =
    topos.getClass.getName.contains(classOf[ConstructToposOfMonoidActions].getSimpleName)

  describe(s"The topos ${topos.getClass.getName}") {

    it("wraps dots and arrows with relatively sane equality semantics") {
      makeSampleDot().sanityTest
      makeSampleDot() shouldBe makeSampleDot()
      (makeSampleDot() eq makeSampleDot()) shouldBe true

      makeSampleArrow().sanityTest
      makeSampleArrow() shouldBe makeSampleArrow()
      (makeSampleArrow() eq makeSampleArrow()) shouldBe false
    }

    it("has identity arrows which can be composed") {
      foo(foo)(identity) shouldBe foo.identity
      foo2bar o foo.identity shouldBe foo2bar
      bar.identity o foo2bar shouldBe foo2bar
    }

    it("can construct biproduct diagrams") {
      (bar x baz).sanityTest
      (bar x baz) should have(
        'left (bar),
        'right (baz)
      )
      val productArrow = foo2bar x foo2baz

      productArrow.sanityTest
      productArrow should have (
        'source (foo),
        'target (bar x baz),
        'sanityTest (null)
      )

      (bar x baz).π0.sanityTest
      (bar x baz).π1.sanityTest

      foo(bar) {
        x => productArrow(x)._1
      } shouldBe foo2bar

      foo(baz) {
        x => productArrow(x)._2
      } shouldBe foo2baz

      val fooXbar = foo x bar
      fooXbar(fooXbar) {
        ⊕ tupled fooXbar.pair
      } shouldBe fooXbar.identity
    }

    it("has a terminator") {
      I.sanityTest
      val fooToI = foo.toI
      fooToI.sanityTest
      fooToI.source shouldBe foo
      fooToI.target shouldBe topos.I

      bar.toI o foo2bar shouldBe fooToI
    }

    it("has a (derived) initial object") {
      O.sanityTest
      val fooFromO = foo.fromO
      fooFromO.sanityTest
      fooFromO.source shouldBe O
      fooFromO.target shouldBe foo

      foo2bar o fooFromO shouldBe bar.fromO
      O >> foo shouldBe Seq(fooFromO)
    }

    it("consistently calculates arrows from the initial to the terminal") {
      O.toI shouldBe I.fromO
    }

    it("has standardized products") {
      (foo x bar) shouldBe (foo x bar)
    }

    it("can chain products") {
      val barXfooXbaz = bar x foo x baz
      val productArrow = foo2bar x foo.identity x foo2baz
      productArrow.sanityTest
      productArrow.source shouldBe foo
      productArrow.target shouldBe barXfooXbaz

      leftProjection(bar, foo, baz) o productArrow shouldBe foo2bar
      midProjection(bar, foo, baz) o productArrow shouldBe foo.identity
      rightProjection(bar, foo, baz) o productArrow shouldBe foo2baz
    }

    it("can construct exponential diagrams") {
      // Check evaluation maps baz^bar x bar -> baz
      val exponential = bar > baz
      exponential.sanityTest
      val evaluation = exponential.evaluation
      evaluation.product.sanityTest
      evaluation.product.left shouldBe (bar > baz)
      evaluation.product.right shouldBe bar
      evaluation.arrow.sanityTest
      evaluation.arrow.target shouldBe baz

      val foo2bar2baz: FOO > (BAR → BAZ) = (bar > baz) transpose foobar2baz
      foo2bar2baz.sanityTest
      foo2bar2baz should have(
        'source(foo),
        'target(bar > baz)
      )

      (foo x bar)(baz) {
        case f ⊕ b =>
          foo2bar2baz(f)(b)
      } shouldBe foobar2baz.arrow
    }

    it("has standardized exponentials") {
      (foo > bar) shouldBe (foo > bar)
    }

    it("has equalizers", Tag("eq")) {
      // minor hackery required to extract the types
      def runTest[S <: ~, M <: ~, T <: ~](
        situation: EqualizerSituation[S, M, T]
      ) {
          import situation._
          val equalizer = s ?= t
          val e = equalizer.inclusion

          (s o e) shouldBe (t o e)
          (e o equalizer.restrict(r)) shouldBe r
      }
      runTest(equalizerSituation)
    }

    it("has a truth object (subobject classifier)") {
      omega.sanityTest
      truth.sanityTest
      truth.source shouldBe I
      truth.target shouldBe omega

      TruthObject.falsity.sanityTest

      val char = monicBar2baz.chi
      char.sanityTest
      char.source shouldBe baz
      char.target shouldBe omega

      char o monicBar2baz shouldBe bar.toTrue

      val restriction = foo2ImageOfBar \ monicBar2baz
      restriction.sanityTest
      restriction.source shouldBe foo
      restriction.target shouldBe bar
      monicBar2baz o restriction shouldBe foo2ImageOfBar

      // Note behaviour is not defined for these pathological cases:
      // construct a non-monic arrow, have chi throw a NotMonicException
      // try backdividing by a monic when we can't
      // It's up to the caller to check. There could be a safe backdivide
    }

    it("has enumeration of globals and arrows") {
      I.globals shouldBe Seq(I.identity)
      (foo x baz).globals.size shouldBe foo.globals.size * baz.globals.size
      foo >> I shouldBe Seq(foo.toI)
      foo >> bar should contain(foo2bar)
      foo >> baz should contain(foo2ImageOfBar)
      bar >> baz should contain(monicBar2baz)
    }

    it("can tell if an arrow is monic") {

      if (!inActionTopos) { // reluctantly skip, too slow with current technology
        monicBar2baz shouldBe 'monic

        (foo x foo).π0 should not be 'monic
        foo.=?=.arrow should not be 'monic

        foo.diagonal shouldBe 'monic
        foo.singleton shouldBe 'monic
      }

      I.identity shouldBe 'monic
      truth shouldBe 'monic
      TruthObject.falsity shouldBe 'monic

      foo.toI should not be 'monic
      foo.fromO shouldBe 'monic
    }

    it("can tell if a arrow is epic") {
  
      I.identity shouldBe 'epic
      O.identity shouldBe 'epic
      I.diagonal shouldBe 'epic
      truth should not be 'epic
      foo.toI shouldBe 'epic

      if (!inActionTopos) { // reluctantly skip, too slow with current technology
        foo.identity shouldBe 'epic
        (foo x foo).π0 shouldBe 'epic
        foo.diagonal should not be 'epic
        omega.diagonal should not be 'epic

        monicBar2baz should not be 'epic
      }
    }

    it("can tell if an arrow is iso and if so, calculate the inverse") {
      val iI = I.identity
      iI shouldBe 'iso
      iI.inverse shouldBe iI

      I.diagonal shouldBe 'iso

      val fooI = foo.identity
      fooI shouldBe 'iso
      fooI.inverse shouldBe fooI

      I -* foo shouldBe 'iso
      foo *- I shouldBe 'iso
    }
  }
}
