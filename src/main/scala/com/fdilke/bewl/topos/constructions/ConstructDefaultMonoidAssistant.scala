package com.fdilke.bewl.topos.constructions

import com.fdilke.bewl.helper.⊕
import com.fdilke.bewl.topos._
import com.fdilke.bewl.topos.algebra.{AlgebraicMachinery, AlgebraicStructures}
import scala.language.higherKinds
import scala.language.reflectiveCalls
import scala.language.postfixOps

trait ConstructDefaultMonoidAssistant extends
  BaseTopos with
  ToposEnrichments {

  Ɛ: AlgebraicStructures with AlgebraicMachinery =>

  trait MonoidAssistant {
    def actionAnalyzer[
      M <: ~
    ] (
      monoid: Monoid[M]
    ): {
      type ANALYSIS[A <: ~] <: monoid.ActionAnalysis[A, ANALYSIS]
      val analyzer: monoid.ActionAnalyzer[ANALYSIS]
    }
  }

  object DefaultMonoidAssistant extends MonoidAssistant {
    override def actionAnalyzer[
      M <: ~
    ] (
      monoid: Monoid[M]
    ): {
      type ANALYSIS[A <: ~] <: monoid.ActionAnalysis[A, ANALYSIS]
      val analyzer: monoid.ActionAnalyzer[ANALYSIS]
    } =
      new Object {
        abstract class DefaultActionAnalysis[A <: ~](
          override val action: monoid.Action[A]
        ) extends monoid.ActionAnalysis[A, DefaultActionAnalysis]

        type ANALYSIS[A <: ~] = DefaultActionAnalysis[A]
        val analyzer: monoid.ActionAnalyzer[ANALYSIS] =
          new monoid.ActionAnalyzer[DefaultActionAnalysis] {
            override def analyze[A <: ~](
              action: monoid.Action[A]
            ) =
              new DefaultActionAnalysis[A](
                action
              ) {
                override def morphismsTo[B <: ~](
                  target: DefaultActionAnalysis[B]
                ): Traversable[A > B] = {
                  val targetAction = target.action
                  val targetCarrier =
                    targetAction.actionCarrier
                  val targetMultiply =
                    targetAction.actionMultiply

                  val product = action.actionCarrier x monoid.carrier
                  action.actionCarrier >> targetCarrier filter { arrow =>
                    (
                      product.biArrow(omega) { (a, m) =>
                        targetCarrier.=?=(
                          arrow(action.actionMultiply(a, m)),
                          targetMultiply(arrow(a), m)
                        )
                      } arrow
                    ) toBool
                  }
                }

                override def rawExponential[B <: ~](
                  target: DefaultActionAnalysis[B]
                ) = {
                  val targetAction = target.action
                  val targetCarrier =
                    targetAction.actionCarrier
                  val targetMultiply =
                    targetAction.actionMultiply

                  val mXs = monoid.carrier x action.actionCarrier
                  val possibleMorphisms =
                    mXs > targetCarrier
                  import possibleMorphisms.{evaluate => $}

                  val morphisms: EQUALIZER[M x A → B] =
                    possibleMorphisms.whereAll(
                      monoid.carrier,
                      monoid.carrier,
                      action.actionCarrier
                    ) {
                      (f, n, m, s) =>
                        targetCarrier.=?=(
                          $(
                            f,
                            mXs.pair(
                              monoid.multiply(m, n),
                              action.actionMultiply(s, n)
                            )
                          ),
                          targetMultiply(
                            $(
                              f,
                              mXs.pair(m, s)
                            ),
                            n
                          )
                        )
                    }

                  val morphismMultiply =
                    morphisms.restrict(
                      possibleMorphisms.transpose(
                        morphisms x monoid.carrier
                      ) {
                        case (f ⊕ m, n ⊕ s) =>
                          $(
                            morphisms.inclusion(f),
                            mXs.pair(
                              monoid.multiply(m, n),
                              s
                            )
                          )
                      }
                    )

                  new monoid.RawExponential[A, B] {
                    override val exponentialAction =
                      monoid.Action[M x A → B](
                        morphisms,
                        BiArrow[M x A → B, M, M x A → B](
                          morphisms x monoid.carrier,
                          morphismMultiply
                        )
                      )
                    override val evaluation =
                      (morphisms x action.actionCarrier).biArrow(
                        targetCarrier
                      ) {
                        (f, s) =>
                          $(
                            f,
                            mXs.pair(
                              monoid.unit(
                                action.actionCarrier.toI(s)
                              ),
                              s
                            )
                          )
                      }

                    override def transpose[X <: ~](
                      otherAction: monoid.Action[X],
                      biArrow: BiArrow[X, A, B]
                    ) =
                      morphisms.restrict(
                        possibleMorphisms.transpose(
                          otherAction.actionCarrier
                        ) {
                          case (x, m ⊕ s) =>
                            biArrow(
                              otherAction.actionMultiply(x, m),
                              s
                            )
                        }
                      )
                  }
                }
              }
          }
    }
  }
  
  val monoidAssistant: MonoidAssistant = 
    DefaultMonoidAssistant    
}