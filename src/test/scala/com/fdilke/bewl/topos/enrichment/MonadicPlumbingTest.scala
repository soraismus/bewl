package com.fdilke.bewl.topos.enrichment

import com.fdilke.bewl.fsets.FiniteSets._
import com.fdilke.bewl.fsets.FiniteSetsUtilities.dot
import org.scalatest.FreeSpec
import org.scalatest.Matchers._

class MonadicPlumbingTest extends FreeSpec {

  "The twist map" - {
    "is calculated properly for sets" in {
      val symbols = dot('A, 'B)
      val numbers = dot(1, 2, 3)

      twist(
        symbols,
        numbers
      ) should have(
        'source(
          symbols x numbers
        ),
        'target(
          numbers x symbols
        ),
        'iso(
          true
        )
      )

      twist(symbols, numbers) o
        twist(numbers, symbols) shouldBe
          (numbers x symbols).identity
    }
  }

  "The associator" - {
    "is calculated properly for sets" in {
      val symbols = dot('A, 'B)
      val numbers = dot(1, 2)
      val strings = dot("foo", "bar")

      associator(
        symbols,
        numbers,
        strings
      ) should have(
        'source(
          (symbols x numbers) x strings
        ),
        'target(
          symbols x (numbers x strings)
        ),
        'iso(
          true
        )
      )
    }
  }

  "The coassociator" - {
    "is calculated properly for sets" in {
      val symbols = dot('A, 'B)
      val numbers = dot(1, 2)
      val strings = dot("foo", "bar")

      coassociator(
        symbols,
        numbers,
        strings
      ) should have(
        'source(
          symbols x (numbers x strings)
        ),
        'target(
          (symbols x numbers) x strings
        ),
        'iso(
          true
        )
      )
    }
  }
}
