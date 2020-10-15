package com.sgib.kata.service.routes

import cats.effect.IO
import com.sgib.kata.service.logging.IOLogging
import com.sgib.kata.service.models._
import com.sgib.kata.service.utils.DB
import com.sgib.kata.service.utils.Queries._
import com.sgib.kata.service.utils.TransactionUtils._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec._

object AccountController extends Http4sDsl[IO] with IOLogging {

  implicit val cs = IO.contextShift(scala.concurrent.ExecutionContext.global)

  def routes(db: DB): HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case req @ POST -> Root / "deposit" =>
        val fund = for {
          depositRequest      <- req.as[DepositRequest]
          account             <- getAccount(depositRequest.accountId)(db)
          accountTransactions <- depositFund(depositRequest)(db)
          currentFund = getCurrentAmount(account.devise, accountTransactions)
        } yield currentFund
        fund.flatMap(f => Ok(f))

      case req @ POST -> Root / "withdrawal" =>
        val tmp = for {
          withdrawalRequest <- req.as[WithdrawalRequest]
          account           <- getAccount(withdrawalRequest.accountId)(db)
          transactions      <- getTransactions(account.accountId)(db)
          currentFund = getCurrentAmount(account.devise, transactions)
          newMinusFund = getNewFund(
            withdrawalRequest.amount,
            withdrawalRequest.devise,
            currentFund,
            account.devise
          )
        } yield (newMinusFund, account.allowedNegative, withdrawalRequest)
        tmp.flatMap {
          case (newMinusFund, allowedNegative, withdrawalRequest) =>
            if (newMinusFund > -allowedNegative) {
              withdrawFund(withdrawalRequest)(db)
              Ok(newMinusFund)
            } else {
              Forbidden()
            }
        }

      case GET -> Root / UUIDVar(accountId) / "funds" =>
        log.info(s"Fetching account funds for id: ${accountId}")
        val ft = for {
          account      <- getAccount(accountId)(db)
          transactions <- getTransactions(accountId)(db)
          currentFunds = getCurrentAmount(account.devise, transactions)
        } yield currentFunds
        ft.flatMap(funds => Ok(funds))

      case GET -> Root / UUIDVar(accountId) / "history" =>
        log.info(s"Fetching account informations for id: ${accountId}")
        val ft = for {
          account      <- getAccount(accountId)(db)
          transactions <- getTransactions(accountId)(db)
          formattedTransactions = getFormattedTransactions(account.devise, transactions)
        } yield formattedTransactions
        ft.flatMap(trx => Ok(trx))
    }
}
