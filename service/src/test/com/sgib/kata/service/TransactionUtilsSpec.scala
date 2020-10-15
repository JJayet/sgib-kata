package com.sgib.kata.service

import cats.effect.{ContextShift, IO, Timer}
import com.sgib.kata.service.utils._
import com.sgib.kata.service.utils.TransactionUtils.getAmountInDevise
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

class TransactionUtilsSpec extends AnyFlatSpecLike with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val t: Timer[IO]         = IO.timer(ExecutionContext.global)

  "getAmountInDevise" should "return the correct amount in devise" in {
    getAmountInDevise(Dollar, Dollar, 1) shouldBe 1
    getAmountInDevise(Euro, Dollar, 1) shouldBe 1.18
    getAmountInDevise(Dollar, Euro, 1) shouldBe 0.8474576271186441
    getAmountInDevise(Yen, Euro, 15000) shouldBe 120.76271186440678
  }
}
