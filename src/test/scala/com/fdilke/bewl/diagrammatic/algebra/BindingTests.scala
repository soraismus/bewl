package com.fdilke.bewl.diagrammatic.algebra

import com.fdilke.bewl.fsets.DiagrammaticFiniteSets
import com.fdilke.bewl.fsets.DiagrammaticFiniteSets.FiniteSetsUtilities._
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class BindingTests  extends FunSpec {
  import DiagrammaticFiniteSets._

  describe("Algebraic operators") {
    it("can wrap arrows, be bound to sources, verify algebraic laws") {

      // 1. Construct the operators; unit and multiplication for a noncommutative monoid

      val (i, x, y) = ('i, 'x, 'y)
      val dot = set(i, x, y)
      val unit = nullaryOperator(dot, i)
      type X = Symbol
      val productOp = FiniteSetsArrow[Power[X], X](
        dot ^ 2, dot, Map(
          (i, i) -> i, (i, x) -> x, (i, y) -> y,
          (x, i) -> x, (x, x) -> x, (x, y) -> x,
          (y, i) -> y, (y, x) -> y, (y, y) -> y
        ))
      val unitOp = FiniteSetsArrow[Power[X], X](
        dot ^ 0, dot, Map(() -> i)
      )

      // 2. Bind them to a power of the carrier as source

      val source: DOT[DiagrammaticFiniteSets.Power[Symbol]] = dot ^ 3
      val boundUnitOp = BoundAlgebraicOperator(source, unitOp)
      val boundProductOp = BoundAlgebraicOperator(source, productOp)

      // 3. Use 'universal variables' to verify algebraic laws

      var Seq(a, b, c) = (dot A 3).projection
      var u = boundUnitOp()
      boundProductOp(a, u) shouldBe a
      boundProductOp(u, a) shouldBe a
      boundProductOp(a, b) should not be (boundProductOp(b, a))
      boundProductOp(boundProductOp(a, b), c) shouldBe boundProductOp(a, boundProductOp(b, c))
    }
  }

  describe("The name of an arrow") {
    it("is calculated correctly for sets, at least") {
      val foo = set(1, 2, 3)
      val bar = set('a, 'b, 'c, 'd)
      val foo2bar = arrow(foo, bar, 1 -> 'a, 2 -> 'c, 3 -> 'a)
      val name = foo2bar.name
      name.source shouldBe I
      name.target shouldBe bar ^ foo
      val unitFunction = name.function.asInstanceOf[Unit => Int => Symbol]
      val namedFunction: Int => Symbol = unitFunction()
      Seq(1,2,3).map(namedFunction) shouldBe Seq('a, 'c, 'a)
    }
  }
}