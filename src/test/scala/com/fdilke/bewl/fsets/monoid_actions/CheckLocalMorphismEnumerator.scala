package com.fdilke.bewl.fsets.monoid_actions

import com.fdilke.bewl.fsets.FiniteSets
import FiniteSets.{Monoid, >, DefaultMonoidAssistant, LocalMonoidAssistant, ToposOfMonoidActions, bifunctionAsBiArrow, functionAsArrow}
import org.scalatest.Matchers._
import com.fdilke.bewl.testutil.CustomMatchers._

object CheckLocalMorphismEnumerator {
  def apply[M, X, Y](
    monoid: Monoid[M]
  ) (
    sourceAction: monoid.Action[X],
    targetAction: monoid.Action[Y],
    thorough: Boolean
  ) {
    val localAnalyzer: monoid.ActionAnalyzer =
      LocalMonoidAssistant.actionAnalyzer(
        monoid
      )

    def enumerateMorphisms(
      analyzer: monoid.ActionAnalyzer
    ): Traversable[X > Y] =
      analyzer.analyze(
        sourceAction
      ).morphismsTo(
        analyzer.analyze(
          targetAction
        )
      )

    val localMorphisms =
      enumerateMorphisms(
        localAnalyzer
      )

    localMorphisms.forall {
      monoid.actions.isMorphism(
        sourceAction,
        targetAction,
        _
      )
    } shouldBe true

    if (thorough) {
      val defaultAnalyzer: monoid.ActionAnalyzer =
        DefaultMonoidAssistant.actionAnalyzer(
          monoid
        )

      val defaultMorphisms =
        enumerateMorphisms(
          defaultAnalyzer
        )

      localMorphisms should not(containDuplicates)
      localMorphisms should contain theSameElementsAs defaultMorphisms
    }
  }
}
