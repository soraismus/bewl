package com.fdilke.bewl.topos

import com.fdilke.bewl.actions.NaiveMonoidsAndActions
import com.fdilke.bewl.helper.Memoize

import scala.Function.tupled
import scala.language.higherKinds

trait Topos extends BaseTopos with NaiveMonoidsAndActions with AlgebraicMachinery with LogicalOperations

trait BaseTopos { self: LogicalOperations =>
  type ~
  type STAR[S <: ~] <: Star[S]
  type QUIVER[S <: ~, T <: ~] <: Quiver[S, T]

  type >[T <: ~, U <: ~] <: (T => U) with ~
  type x[T <: ~, U <: ~] <: (T, U) with ~

  type UNIT <: ~
  val I : STAR[UNIT]

  type TRUTH <: ~
  val omega: STAR[TRUTH]
  val truth: QUIVER[UNIT, TRUTH]

  implicit class OmegaEnrichments(truthValue: TRUTH) {
    def >(that: TRUTH) = TruthObject.implies(truthValue, that)
    def ^(that: TRUTH) = TruthObject.and(truthValue, that)
    def v(that: TRUTH) = TruthObject.or(truthValue, that)
  }

  type EXPONENTIAL[S <: ~, T <: ~] = ExponentialStar[S, T, S > T] with STAR[S > T]
  trait ExponentialStar[S <: ~, T <: ~, S_T <: (S => T) with ~] { star: STAR[S_T] =>
    val source: STAR[S]
    val target: STAR[T]

    def transpose[R <: ~](biQuiver: BiQuiver[R, S, T]): QUIVER[R, S_T]
    final def evaluation: BiQuiver[S_T, S, T] =
      (this x source).biQuiver(target) { (f, s) => f(s) }
  }

  type BIPRODUCT[L <: ~, R <: ~] = BiproductStar[L, R, L x R] with STAR[L x R]

  trait BiproductStar[L <: ~, R <: ~, LxR <: (L, R) with ~] { star: STAR[LxR] =>
    val left: STAR[L]
    val right: STAR[R]
    def pair(l: L, r: R): LxR
    final lazy val π0 = star(left) { _._1 }
    final lazy val π1 = star(right) { _._2 }

    final private val hackedThis: BIPRODUCT[L, R] = this.asInstanceOf[BIPRODUCT[L, R]]

    final def biQuiver[T <: ~](
      target: STAR[T]
      ) (
      bifunc: (L, R) => T
      ) : BiQuiver[L, R, T] =
      BiQuiver(hackedThis, hackedThis(target) (
        tupled[L,R,T](bifunc)
      ))
      final def universally[T <: ~](target: STAR[T])(bifunc: ((L x R), T) => TRUTH) =
        BiQuiver(hackedThis, target.forAll(hackedThis)(bifunc))
      final def existentially[T <: ~](target: STAR[T])(bifunc: ((L x R), T) => TRUTH) =
        BiQuiver(hackedThis, target.exists(hackedThis)(bifunc))
    }

  type EQUALIZER[S <: ~] = EqualizingStar[S] with STAR[S]
  trait EqualizingStar[S <: ~] { star: STAR[S] =>
    val equalizerTarget: STAR[S]
    val inclusion: QUIVER[S, S]
    def restrict[R <: ~](quiver: QUIVER[R, S]): QUIVER[R, S]
  }

  trait BaseStar[S <: ~] { self: STAR[S] =>
    val toI: QUIVER[S, UNIT]
    val globals: Traversable[QUIVER[UNIT, S]]
    def xUncached[T <: ~](that: STAR[T]): BIPRODUCT[S, T]
    def `>Uncached`[T <: ~](that: STAR[T]): EXPONENTIAL[S, T]
    def apply[T <: ~](target: STAR[T])(f: S => T) : QUIVER[S, T]
    def sanityTest
  }

  trait Star[S <: ~] extends BaseStar[S] { star: STAR[S] =>

    final lazy val identity: QUIVER[S, S] = this(star) { s => s }
    final private val memoizedProduct =
      Memoize.generic.withLowerBound[
        STAR,
        ({ type λ[U <: ~] = BIPRODUCT[S, U]})#λ,
        ~
      ] (xUncached)

    final def x[U <: ~](that: STAR[U]): BIPRODUCT[S, U] = memoizedProduct(that)

    final private val memoizedExponential =
      Memoize.generic.withLowerBound[
        STAR,
        ({ type λ[T <: ~] = EXPONENTIAL[S, T]})#λ,
        ~
      ] (`>Uncached`)

    final def >[T <: ~](that: STAR[T]): EXPONENTIAL[S, T] = memoizedExponential(that)

    final lazy val toTrue = truth o toI
    final lazy val power = this > omega
    final lazy val ∀ = toTrue.name.chi
    final lazy val squared = this x this

    final def map(f: S => S) = this(this)(f)
    final def flatMap(f2: S => QUIVER[S, S]) =
      (this x this).biQuiver(this) { f2(_)(_) }

    final lazy val ∃ =
      omega.forAll(power) { (f, w) =>
          (power x omega).universally(this) {
            case ((f, w), x) => f(x) > w
          }(f, w) > w
      }

    final def forAll[R <: ~](source: STAR[R])(g: (R, S) => TRUTH): QUIVER[R, TRUTH] =
      ∀ o power.transpose(
        (source x this).biQuiver(omega)(g)
      )

    final def exists[R <: ~](source: STAR[R])(g: (R, S) => TRUTH): QUIVER[R, TRUTH] =
      ∃ o power.transpose(
        (source x this).biQuiver(omega)(g)
      )

    final lazy val diagonal: BiQuiver[S, S, TRUTH] =
      BiQuiver(squared, this(squared) { x => squared.pair(x, x) }.chi)

    final def >>[T <: ~](
      target: STAR[T]
    ): Traversable[
      QUIVER[S, T]
    ] =
      (this > target).globals map { global =>
        star(target) { s =>
          global(star.toI(s))(s)
        }}
  }

  trait BaseQuiver[S <: ~, T <: ~] {
    val source: STAR[S]
    val target: STAR[T]
    val chi: QUIVER[T, TRUTH]

    def apply(s: S): T
    def ?=(that: QUIVER[S, T]): EQUALIZER[S]
    def o[R <: ~](that: QUIVER[R, S]) : QUIVER[R, T]
    def \[U <: ~](monic: QUIVER[U, T]) : QUIVER[S, U]
    def sanityTest
  }

  trait Quiver[S <: ~, T <: ~] extends BaseQuiver[S, T] { self: QUIVER[S, T] =>
    final def name =
      (source > target).transpose(
        (I x source).biQuiver(target) {
          (i, x) => this(x)
        })
    final def x[U <: ~](that: QUIVER[S, U]): QUIVER[S, T x U] = {
      val product = target x that.target
      source(product) {
        s => product.pair(this(s), that(s))
      }}
  }

  case class BiQuiver[
    L <: ~,
    R <: ~,
    T <: ~](
    product: BIPRODUCT[L, R],
    quiver: QUIVER[L x R, T]) {
    def apply(l: L, r: R): T = quiver(product.pair(l, r))
    def apply[S <: ~](l: QUIVER[S, L], r: QUIVER[S, R]): QUIVER[S, T] = quiver o (l x r)
  }

  // Helper methods for triproducts (this could obviously be extended).
  def leftProjection[X <: ~, Y <: ~, Z <: ~](
    x: STAR[X], y: STAR[Y], z: STAR[Z]
  ) : QUIVER[X x Y x Z, X] =
    (x x y).π0 o (x x y x z).π0

  def midProjection[X <: ~, Y <: ~, Z <: ~](
   x: STAR[X], y: STAR[Y], z: STAR[Z]
  ) : QUIVER[X x Y x Z, Y] =
    (x x y).π1 o (x x y x z).π0

  def rightProjection[X <: ~, Y <: ~, Z <: ~](
   x: STAR[X], y: STAR[Y], z: STAR[Z]
  ) : QUIVER[X x Y x Z, Z] =
    (x x y x z).π1
}

trait Wrappings[BASE, PRESTAR[_ <: BASE], PREQUIVER[_ <: BASE, _ <: BASE]] { topos: BaseTopos =>
  type WRAPPER[T <: BASE] <: ~

  def star[T <: BASE](input: PRESTAR[T]) : STAR[WRAPPER[T]]
  def quiver[S <: BASE, T <: BASE](connector: PREQUIVER[S, T]) : QUIVER[WRAPPER[S], WRAPPER[T]]
  def functionAsQuiver[S <: BASE, T <: BASE](source: STAR[WRAPPER[S]], target: STAR[WRAPPER[T]], f: S => T): QUIVER[WRAPPER[S], WRAPPER[T]]
  def bifunctionAsBiQuiver[L <: BASE, R <: BASE, T <: BASE] (
    left: STAR[WRAPPER[L]],
    right: STAR[WRAPPER[R]],
    target: STAR[WRAPPER[T]]
  ) (
    bifunc: (L, R) => T
  ): BiQuiver[WRAPPER[L], WRAPPER[R], WRAPPER[T]]

  def bifunctionAsBiQuiver[X <: BASE] (
    star: STAR[WRAPPER[X]]
  ) (
     bifunc: (X, X) => X
  ): BiQuiver[WRAPPER[X], WRAPPER[X], WRAPPER[X]] =
    bifunctionAsBiQuiver[X, X, X](star, star, star) { bifunc }
}


