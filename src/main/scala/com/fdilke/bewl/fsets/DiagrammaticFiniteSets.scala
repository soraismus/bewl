package com.fdilke.bewl.fsets

import com.fdilke.bewl.diagrammatic.DiagrammaticTopos
import com.fdilke.bewl.fsets.DiagrammaticFiniteSetsUtilities.allMaps
import com.fdilke.bewl.helper.FunctionWithEquality
import com.fdilke.bewl.topos.StarsAndQuiversAdapter
import scala.Function.{const, tupled}

object DiagrammaticFiniteSets extends DiagrammaticTopos {
  case class FiniteSetsDot[X](elements: Traversable[X]) extends Dot[X] with Traversable[X] {
    override def toString = elements.toString

    override def foreach[U](f: (X) => U) { elements.foreach(f) }

    override def identity: FiniteSetsArrow[X, X] = FiniteSetsArrow(this, this, x => x)

    override def multiply[Y](that: FiniteSetsDot[Y]) =
      new Biproduct[X, Y] {
        override val product = FiniteSetsDot[(X, Y)](for (x <- FiniteSetsDot.this; y <- that) yield (x, y))

        override val leftProjection = FiniteSetsArrow[(X, Y), X](product, FiniteSetsDot.this, tupled { (x, y)  => x} )

        override val rightProjection = FiniteSetsArrow[(X, Y), Y](product, that, tupled { (x, y) => y})

        override def multiply[W](leftArrow: FiniteSetsArrow[W, X], rightArrow: FiniteSetsArrow[W, Y]) =
          FiniteSetsArrow(leftArrow.source, product, x => (leftArrow.function(x), rightArrow.function(x)) )
    }

    override def exponential[Y](that: FiniteSetsDot[Y]) =
      new Exponential[Y, X] {
        // the 'function equality' semantics are needed even if these are all maps, because
        // we'll be comparing against things that aren't
        val theAllMaps: Traversable[Y => X] = allMaps(that, FiniteSetsDot.this).
          map(FunctionWithEquality(that, _))
        override val exponentDot: FiniteSetsDot[Y => X] = FiniteSetsDot[Y => X](theAllMaps)

        override val evaluation = new BiArrow[Y => X, Y, X](exponentDot, that,
          FiniteSetsArrow[(Y => X, Y), X](exponentDot x that, FiniteSetsDot.this, tupled {
            (f:Y => X, y:Y) => f(y)
          }))

        override def transpose[W](multiArrow: BiArrow[W, Y, X]) =
          FiniteSetsArrow[W, Y => X](multiArrow.left, exponentDot,
            (w: W) => FunctionWithEquality[Y, X](multiArrow.right, { s => multiArrow.arrow.function((w, s)) })
          )
      }


    override def toI = FiniteSetsArrow[X, TERMINAL](this, I, const () _)

    override def sanityTest =
      for (x <- elements ; y <- elements)
        x == y
  }

  case class FiniteSetsArrow[X, Y](
    source: FiniteSetsDot[X],
    target: FiniteSetsDot[Y],
    function: X => Y
  ) extends Arrow[X, Y] {
    override def toString = s"""FiniteSetsArrow(${source}, ${target},
      |${Map(source.map(x => (x, function(x))).toList: _*)})""".stripMargin

    override def apply[W](arrow: FiniteSetsArrow[W, X]) =
      if (arrow.target == source) {
        FiniteSetsArrow(arrow.source, target,  function.compose(arrow.function))
      } else {
        throw new IllegalArgumentException("Target does not match source")
      }

    override def ?=(that: FiniteSetsArrow[X, Y]) =
      new Equalizer[X] {

      override val equalizerSource = FiniteSetsDot[EQUALIZER_SOURCE[X]](
        source.filter(s => function(s) == that.function(s))
      )

      override val equalizer = FiniteSetsArrow[EQUALIZER_SOURCE[X], X](
        equalizerSource, source, identity
      )

      override def restrict[S](equalizingArrow: FiniteSetsArrow[S, X]) = FiniteSetsArrow(
        equalizingArrow.source, equalizerSource, equalizingArrow.function
      )
    }

    override lazy val chi = new Characteristic[X, Y] {
      val arrow = FiniteSetsArrow[Y, Boolean](target, omega,
        (y: Y) => source.exists(function(_) == y)
      )
      def restrict[W](arrow: ARROW[W, Y]) = FiniteSetsArrow[W, X](
        arrow.source, source, (w: W) => {
          val y = arrow.function(w)
          source.find(function(_) == y).getOrElse(???)
        })
    }

    override def equals(other: Any): Boolean = other match {
      case that: FiniteSetsArrow[X, Y] =>
        source == that.source && target == that.target &&
          source.forall(x => function(x) == that.function(x))
      case _ => false
    }

    override def hashCode(): Int = 0 // don't use these as keys

    def sanityTest =
      if (source.map(function).exists(x => target.forall(_ != x))) {
        throw new IllegalArgumentException("Map values not in target")
      }
  }

  object FiniteSetsBiArrow {
    def apply[L, R, T](left: FiniteSetsDot[L],
                       right: FiniteSetsDot[R],
                       target: FiniteSetsDot[T],
                       map: ((L, R), T)*) : BiArrow[L, R, T] =
      BiArrow[L, R, T](left, right,
        FiniteSetsArrow[(L, R), T](left x right, target, Map(map:_*)))
  }

  override type DOT[X] = FiniteSetsDot[X]
  override type ARROW[S, T] = FiniteSetsArrow[S, T]
  override type EQUALIZER_SOURCE[M] = M
  override type TERMINAL = Unit
  override type OMEGA = Boolean

  override val I = FiniteSetsDot[Unit](Traversable(()))
  override val omega = FiniteSetsDot[Boolean](Traversable(true, false))
  override val truth = FiniteSetsArrow[Unit, Boolean](I, omega, const (true) _)

  override def buildArrow[S, T](source: FiniteSetsDot[S], target: FiniteSetsDot[T], f: S => T) =
    FiniteSetsArrow(source, target, f)
}

object FiniteSets extends StarsAndQuiversAdapter(DiagrammaticFiniteSets)