package com.fdilke.bewl.fsets.monoid_actions

import com.fdilke.bewl.fsets.{BaseFiniteSets, FiniteSets}
import com.fdilke.bewl.helper.{BuildEquivalence, ⊕}
import com.fdilke.bewl.helper.⊕._

import scala.Function.tupled
import scala.collection.immutable
import scala.language.{higherKinds, postfixOps, reflectiveCalls}

trait ActionSplitter extends BaseFiniteSets {
  Ɛ: FindGenerators =>

//  trait ActionComponent[A, ACTION[B]] {
//    val action: ACTION[A]
//    val generators: Seq[A]
//  }
//
//  trait ActionSplitting[A, ACTION[B]] {
//  }

  object ActionSplitter {
    def forMonoid[M](
      monoid: Monoid[M]
    ): {
      def splitAction[A](
        action: monoid.Action[A]
      ): {
        val allGenerators: Seq[A]
        val components: Seq[
          {
            val componentAction: monoid.Action[A]
            val componentGenerators: Seq[A]
          }
        ]
      }
    } =
      new Object {
        private val monoidElements =
          monoid.carrier.elements

        private val findGenerators =
          FindGenerators forMonoid monoid

        def splitAction[A](
          action: monoid.Action[A]
        ) =
          new Object {
            val allGenerators =
              findGenerators apply(
                action
              ) generators

            val components: Seq[
              {
                val componentAction: monoid.Action[A]
                val componentGenerators: Seq[A]
              }
            ] = {
              val indexedGenerators =
                allGenerators.zipWithIndex
              val actionMultiply =
                action.actionMultiply

              val generatorSorts =
                BuildEquivalence(
                  allGenerators.size,
                  for {
                    (g, i) <- indexedGenerators
                    (h, j) <- indexedGenerators
                    m <- monoidElements
                    n <- monoidElements
                    if actionMultiply(g, m) ==
                      actionMultiply(h, n)
                  }
                    yield i -> j
                )

              allGenerators.indices.groupBy(
                generatorSorts
              ).values map { block =>
                  new Object {
                    val componentGenerators: Seq[A] =
                      block map allGenerators

                    val componentAction: monoid.Action[A] =
                      monoid.action(
                        makeDot(
                          for {
                            cg <- componentGenerators
                            m <- monoidElements
                          } yield
                            actionMultiply(cg, m)
                        )
                      ) (
                          actionMultiply
                        )
                  }
              } toSeq
            }
          }
      }
  }
}