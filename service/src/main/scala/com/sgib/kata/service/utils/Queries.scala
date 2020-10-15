package com.sgib.kata.service.utils

import java.util.{Date, UUID}

import cats.effect.IO
import com.sgib.kata.service.models.{AccountNotFoundException, DepositRequest, WithdrawalRequest}

object Queries {
  def getAccount(accountId: UUID)(db: DB): IO[Account] =
    IO.fromOption(db.accounts.find(_.accountId == accountId))(AccountNotFoundException)

  def withdrawFund(withdrawalRequest: WithdrawalRequest)(db: DB): IO[Seq[Transaction]] = {
    db.transactions = db.transactions :+
      Transaction(
        withdrawalRequest.accountId,
        new Date(),
        withdrawalRequest.devise,
        withdrawalRequest.amount,
        Withdrawal
      )

    getTransactions(withdrawalRequest.accountId)(db)
  }

  def depositFund(depositRequest: DepositRequest)(db: DB): IO[Seq[Transaction]] = {
    db.transactions = db.transactions :+
      Transaction(
        depositRequest.accountId,
        new Date(),
        depositRequest.devise,
        depositRequest.amount,
        Deposit
      )

    getTransactions(depositRequest.accountId)(db)
  }

  def getTransactions(accountId: UUID)(db: DB): IO[Seq[Transaction]] =
    IO.pure(db.transactions.filter(_.accountId == accountId).sortBy(_.date).reverse)
}
