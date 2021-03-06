package com.fdilke.bewl.testutil

import org.scalatest.matchers.{MatchResult, Matcher}

object CustomMatchers {
  def containDuplicates[T] =
    new Matcher[Traversable[T]]{
      override def apply(collection: Traversable[T]) = {
        val failureMessageSuffix =
          "collection contains duplicates: " + collection

        val negatedFailureMessageSuffix =
          "collection does not contain duplicates: " + collection

        MatchResult(
          collection.toSet.size != collection.size,
          "The " + failureMessageSuffix,
          "The " + negatedFailureMessageSuffix,
          "the " + failureMessageSuffix,
          "the " + negatedFailureMessageSuffix
        )
      }
    }
}
