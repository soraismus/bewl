package com.fdilke.bewl.topos

trait Topos {
  type ELEMENT
  type STAR[S <: ELEMENT] <: Star[S]
  type QUIVER[S <: ELEMENT, T <: ELEMENT] <: Quiver[S, T]

  type x[T <: ELEMENT, U <: ELEMENT] <: xI[T, U] with ELEMENT

  trait xI[T <: ELEMENT, U <: ELEMENT] {
    val left: T
    val right: U
  }

  trait Star[S <: ELEMENT] {
    val identity: QUIVER[S, S]
    def x[T <: ELEMENT](that: STAR[T]): STAR[S x T]
    def sanityTest
  }

  trait Quiver[S <: ELEMENT, T <: ELEMENT] {
    val source: STAR[S]
    val target: STAR[T]

    def o[R <: ELEMENT](that: QUIVER[R, S]) : QUIVER[R, T]
    def x[U <: ELEMENT](that: QUIVER[S, U]): QUIVER[S, T x U]
    def sanityTest
  }

  def bind[S <: ELEMENT, T <: ELEMENT](source: STAR[S], target: STAR[T], f: S => T) : QUIVER[S, T]
  // TODO rename as a proper operator: ":>()()" ?

  // TODO extras - separate into a trait?

  def leftProjection[A <: ELEMENT, B <: ELEMENT](left: STAR[A], right: STAR[B]) =
    bind[A x B, A](left x right, left, _.left)

  def rightProjection[A <: ELEMENT, B <: ELEMENT](left: STAR[A], right: STAR[B]) =
    bind[A x B, B](left x right, right, _.right)
}

trait Wrappings { topos: Topos =>
  type DOTINPUT[T]
  type CONNECTOR[S, T]
  type DOTWRAPPER[T] <: ELEMENT

  def makeStar[T](input: DOTINPUT[T]) : STAR[DOTWRAPPER[T]]
  def makeQuiver[S, T](connector: CONNECTOR[S, T]) : QUIVER[DOTWRAPPER[S], DOTWRAPPER[T]]

  // TODO: rename to star, quiver
}