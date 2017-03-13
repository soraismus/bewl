
package com.fdilke.bewl.topos.algebra.constructions

import com.fdilke.bewl.fsets.FiniteSetsUtilities._
import com.fdilke.bewl.fsets.{FiniteSets, FiniteSetsUtilities}
import com.fdilke.bewl.topos.{GenericToposTests, ToposWithFixtures}
import org.scalatest.Matchers._
import FiniteSets.~

import scala.Function.untupled

class ToposOfMonoidActionsAltTest extends GenericToposTests[~](
  new ToposWithFixtures[~] {

    private val (i, x, y) = ('i, 'x, 'y)

    val monoidOf3 =
      monoidFromTable(
        i, x, y,
        x, x, y,
        y, x, y
      ) // right-dominant on two generators

    override val topos = 
      FiniteSets.ToposOfMonoidActionsAlt of monoidOf3

    import topos._

    override type FOO = Symbol
    override type BAR = String
    override type BAZ = String

    override val foo = makeDot(monoidOf3.regularAction)

    private val barDot: FiniteSets.DOT[String] = FiniteSetsUtilities.dot("x", "y")
    private val bazDot: FiniteSets.DOT[String] = FiniteSetsUtilities.dot("i", "x", "y")

    private val scalarMultiply: (String, Symbol) => String =
      (s, m) => monoidOf3.multiply(Symbol(s), m).name

    override val bar = makeDot(monoidOf3.action(barDot)(scalarMultiply))
    override val baz = makeDot(monoidOf3.action(bazDot)(scalarMultiply))

    override val foo2ImageOfBar = functionAsArrow(foo, baz, Map(i -> "y", x -> "x", y -> "y"))
    override val foo2bar = functionAsArrow(foo, bar, Map(i -> "x", x -> "x", y -> "y"))
    override val foobar2baz = bifunctionAsBiArrow(foo, bar, baz)(untupled (Map(
      (i, "x") -> "x", (x, "x") -> "x", (y, "x") -> "y",
      (i, "y") -> "y", (x, "y") -> "x", (y, "y") -> "y"
    )))
    override val monicBar2baz = functionAsArrow(bar, baz, Map("x" -> "x", "y" -> "y"))

    override def makeSampleDot(): DOT[String] =
      makeDot(monoidOf3.action(barDot)(scalarMultiply))

    override def makeSampleArrow():
      Symbol > String =
      functionAsArrow(foo, bar, Map(
        i -> "x",
        x -> "x",
        y -> "y"
      )) // left multiplication by x

    override val equalizerSituation = {
      val wizDot: FiniteSets.DOT[Int] = FiniteSetsUtilities.dot(0, 1, 2, 3)

      def wizMultiply(n: Int, r: Symbol) : Int =
        if (n == 0)
          0
        else r match {
          case `i` => n
          case `x` => 1
          case `y` => 2
        }

      val wizAction = monoidOf3.action(wizDot)(wizMultiply)

      val wiz = makeDot(wizAction)

      val foo2wiz = 
        functionAsArrow(foo, wiz, Map(
          'i -> 1, 'x -> 1, 'y -> 2
        ))

      type WIZ = Int
      type BINARY = Boolean
      val binaryDot : FiniteSets.DOT[Boolean] = 
        FiniteSetsUtilities.dot(true, false)
      def binaryMultiply(b: Boolean, r: Symbol) : Boolean = b
      val binary = makeDot(
        monoidOf3.action(binaryDot)(binaryMultiply)
      )
      new EqualizerSituation[FOO, WIZ, BINARY](
        foo2wiz,
        functionAsArrow(wiz, binary, Map(
          0 -> true, 1 -> true, 2 -> true, 3 -> true
        )),
        functionAsArrow(wiz, binary, Map(
          0 -> false, 1 -> true, 2 -> true, 3 -> true
        ))
      )
    }
  }
) {
  import fixtures._
  import topos._

  describe("The Boolean property") {
    ignore("fails") { // too slow :( :( :(
      topos should not be 'boolean
    }
  }

  describe("Global element enumeration") {
    it("works on the built-ins") {
      omega.globals should have size 2
    }

    it("works on the fixtures") {
      foo.globals shouldBe 'empty
      bar.globals shouldBe 'empty
      baz.globals shouldBe 'empty
    }
  }

  describe("Arrow enumeration") {
    // too slow! Belongs in a worksheet or app (pending optimization)
    it("also works on the fixtures") {
      (omega >> omega) should have size 6
    }

    ignore("...optional extras") {
      foo >> foo should have size 3
      foo >> bar should have size 2
      foo >> baz should have size 3

      // probably not - that would be if we were only counting isomorphisms
      (foo >> (omega > omega)) should have size 2
    }
  }
}