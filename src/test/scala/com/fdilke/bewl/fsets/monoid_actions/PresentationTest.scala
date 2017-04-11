package com.fdilke.bewl.fsets.monoid_actions;

import com.fdilke.bewl.fsets.FiniteSets
import FiniteSets.{ ToposOfMonoidActions, x }
import com.fdilke.bewl.fsets.FiniteSetsUtilities._
import org.scalatest.FreeSpec
import org.scalatest.Matchers._
import scala.language.reflectiveCalls
import scala.language.existentials

import FiniteSets.{>, VOID, ToposOfMonoidActions}

class PresentationTest extends FreeSpec {

  private val (i, x, y) = ('i, 'x, 'y)
  
  private val monoidOf3 =
      monoidFromTable(
        i, x, y,
        x, x, y,
        y, x, y
      ) // right-dominant on two generators

  private val actionTopos = 
      ToposOfMonoidActions of monoidOf3
      
  "Building actions from presentations" - {
		"works for an empty generating set" in {
			val presentation = 
			  Presentation(
		      monoidOf3
	      )(
		      List[GeneratorWithRelators[Symbol, VOID]]()
	      )

      val emptyAction: monoidOf3.Action[actionTopos.VOID] =
        actionTopos.unwrap(
          actionTopos.O
        )	      
	    val emptyProjection: Int > actionTopos.VOID = 
	      presentation.project(
          emptyAction,
          List[actionTopos.VOID]()
        )
      emptyProjection should have(
        'source(presentation.action.actionCarrier),
        'target(emptyAction.actionCarrier)
      )
	    monoidOf3.actions.isMorphism(
        presentation.action,
        emptyAction,
        emptyProjection
      ) shouldBe true
		}
		"works for a single generator with no relators" in {
			val presentation = 
			  Presentation(
		      monoidOf3
	      )(
		      List[GeneratorWithRelators[Symbol, FiniteSets.UNIT]](
	          GeneratorWithRelators(
              (),
	            Seq.empty
            )
          )
	      )

      val regularAction =
        monoidOf3.regularAction
	    val regularProjection: Int > Symbol = 
	      presentation.project(
          regularAction,
          List(
            i
          )
        )
      regularProjection should have(
        'source(presentation.action.actionCarrier),
        'target(regularAction.actionCarrier)
      )
//	    println("presentation.action =" + presentation.action.actionCarrier)
//	    for {
//	      s <- elementsOf(presentation.action.actionCarrier)
//	      m <- elementsOf(monoidOf3.carrier)
//	    } {
//	      println(s"$s * $m = ${ presentation.action.actionMultiply(s, m) }")
//	    }
//	    println("===========")
	    monoidOf3.actions.isMorphism(
        presentation.action,
        regularAction,
        regularProjection
      ) shouldBe true
      regularProjection shouldBe 'iso
		}
		"works for presenting a cyclic right ideal { x, y }" in {
			val presentation = 
			  Presentation(
		      monoidOf3
	      )(
		      List[GeneratorWithRelators[Symbol, FiniteSets.UNIT]](
	          GeneratorWithRelators(
              (),
              Seq(
                Relator(x, 0, i)
              )        
            )
          )
	      )

		  val idealCarrier = dot(x, y)
		  val idealAction = 
		    monoidOf3.action(idealCarrier) {
		      monoidOf3.multiply(_, _)
		    }
	      
	    val idealProjection: Int > Symbol = 
	      presentation.project(
          idealAction,
          List(
            x
          )
        )
      idealProjection should have(
        'source(presentation.action.actionCarrier),
        'target(idealCarrier)
      )
	    monoidOf3.actions.isMorphism(
        presentation.action,
        idealAction,
        idealProjection
      ) shouldBe true
      idealProjection shouldBe 'iso
		}
		"works for presenting 2 over its endomorphism monoid" in {
		  val ¬ = '¬
		  val O = 'O
		  val I = 'I
      val end2 =
        monoidFromTable(
          i, ¬, O, I,
          ¬, i, O, I,
          O, I, O, I,
          I, O, O, I
        ) 
      
			val presentation = 
			  Presentation(
		      end2
	      )(
		      List[GeneratorWithRelators[Symbol, Int]](
	          GeneratorWithRelators(
              0,
              Seq(
                Relator(O, 0, i),
                Relator(¬, 0, I)
              )        
            )
          )
	      )

		  val twoCarrier = dot(0, 1)
		  val twoActionMap: Map[Symbol, Int => Int] =
		    Map(
		        i -> identity,
		        ¬ -> { 1 - _ },
		        O -> { _ => 0 },
		        I -> { _ => 1 }
        )
		  val twoAction = 
		    end2.action(twoCarrier) {
		      (s, m) => 
		        twoActionMap(m)(s)
		    }
	      
	    val twoProjection: Int > Int = 
	      presentation.project(
          twoAction,
          List(
            0
          )
        )
      twoProjection should have(
        'source(presentation.action.actionCarrier),
        'target(twoCarrier)
      )
	    end2.actions.isMorphism(
        presentation.action,
        twoAction,
        twoProjection
      ) shouldBe true
      twoProjection shouldBe 'iso
		}
  }
}
