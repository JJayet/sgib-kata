package com.sgib.kata.service

import java.util.UUID

import io.circe.generic.auto._
import cats.effect.{ContextShift, IO, Timer}
import com.sgib.kata.service.Config.Config
import com.sgib.kata.service.models._
import com.sgib.kata.service.utils._
import org.http4s._
import org.http4s.client.blaze.BlazeClientBuilder
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import pureconfig.ConfigSource
import org.http4s.circe.CirceEntityCodec._

import scala.concurrent.ExecutionContext

class ServiceIT extends AnyFlatSpecLike with Matchers {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val t: Timer[IO]         = IO.timer(ExecutionContext.global)

  val conf: Config    = ConfigSource.default.loadOrThrow[Config]
  val serverUrl       = s"http://${conf.server.host}:${conf.server.port}"
  val accountId: UUID = UUID.fromString("03f1cfeb-676f-47b2-bd2d-fae6be0520a3")

  val withdrawRequest: Request[IO] =
    Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"$serverUrl/accounts/withdrawal"))

  val depositRequest: Request[IO] =
    Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"$serverUrl/accounts/deposit"))

  val getAccountFundsRequest: Request[IO] =
    Request[IO](
      method = Method.GET,
      uri = Uri.unsafeFromString(s"$serverUrl/accounts/$accountId/funds")
    )

  val getAccountHistoryRequest: Request[IO] =
    Request[IO](
      method = Method.GET,
      uri = Uri.unsafeFromString(s"$serverUrl/accounts/$accountId/history")
    )

  IOAssertion {
    BlazeClientBuilder[IO](ExecutionContext.global).resource.use { client =>
      for {
        accountFunds <- client.expect[Double](getAccountFundsRequest)
        firstWithdrawal <-
          client.expect[Double](withdrawRequest.withEntity(WithdrawalRequest(accountId, 50, Euro)))
        firstDeposit <-
          client.expect[Double](depositRequest.withEntity(DepositRequest(accountId, 30, Euro)))
        secondDeposit <-
          client.expect[Double](depositRequest.withEntity(DepositRequest(accountId, 10, Dollar)))
        lastAccountFunds <- client.expect[Double](getAccountFundsRequest)
        accountHistory   <- client.expect[List[TransactionResult]](getAccountHistoryRequest)
        secondWithdrawal <-
          client.status(withdrawRequest.withEntity(WithdrawalRequest(accountId, 5000, Euro)))
      } yield {
        accountFunds shouldBe 100

        it should "have a balance of 50 after the first withdrawal" in {
          firstWithdrawal shouldBe 50
        }

        it should "have a balance of 80 after the first deposit" in {
          firstDeposit shouldBe 80
        }

        it should "have a balance of ~88 after the second deposit" in {
          secondDeposit shouldBe 88.47
        }

        lastAccountFunds shouldBe secondDeposit

        accountHistory.size shouldBe 5

        secondWithdrawal.code shouldBe 403
      }
    }
  }
}
