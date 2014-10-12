package com.fdilke.bewl.fsets

import com.fdilke.bewl.topos.{Wrappings, Topos}

class NativeFiniteSets extends Topos
  with Wrappings[Traversable, Function] {

  override type ELEMENT = Any
  override type STAR[S <: ELEMENT] = FiniteSetsStar[S]
  override type QUIVER[S <: ELEMENT, T <: ELEMENT] = FiniteSetsQuiver[S, T]
  override type UNIT = Unit
  override type TRUTH = Boolean
  override val I: STAR[UNIT] = ???
  override val omega: STAR[TRUTH] = ???
  override val truth: QUIVER[UNIT, TRUTH] = ???

  class FiniteSetsStar[S] extends Star[S] {
    override val toI: QUIVER[S, UNIT] = ???

    override def >[T <: ELEMENT](that: STAR[T]) = ???

    override def sanityTest: Unit = ???

    override def x[T <: ELEMENT](that: STAR[T]): STAR[x[S, T]] = ???

    override def apply[T <: ELEMENT](target: STAR[T])(f: (S) => T): QUIVER[S, T] = ???
  }

  class FiniteSetsQuiver[S, T] extends Quiver[S, T] {
    override val source: STAR[S] = ???

    override def \[U <: ELEMENT](monic: QUIVER[U, T]): QUIVER[S, U] = ???

    override def sanityTest: Unit = ???

    override def ?=(that: QUIVER[S, T]): EqualizingStar[S] with STAR[EqualizingElement[S] with ELEMENT] = ???

    override def x[U <: ELEMENT](that: QUIVER[S, U]): QUIVER[S, x[T, U]] = ???

    override def apply(s: S): T = ???

    override def o[R <: ELEMENT](that: QUIVER[R, S]): QUIVER[R, T] = ???

    override val chi: QUIVER[T, TRUTH] = ???
    override val target: STAR[T] = ???
  }

  // wrapping layer

  override type WRAPPER[T] = T

  override def functionAsQuiver[S, T](source: STAR[S], target: STAR[T], f: S => T): QUIVER[S, T] = ???

  override def quiver[S, T](connector: Function1[S, T]) = ???

  override def star[T](input: Traversable[T]) = ???

  override def bifunctionAsBiQuiver[L, R, T](left: STAR[L], right: STAR[R], target: STAR[T], bifunc: (L, R) => T): BiQuiver[L, R, T] = ???
}
