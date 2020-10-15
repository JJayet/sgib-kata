package com.sgib.kata.service.utils

import java.text.SimpleDateFormat

import com.sgib.kata.service.models.TransactionResult

object TransactionUtils {

  private def round(value: Double) = (math rint value * 100) / 100

  def getAmountInDevise(from: Devise, to: Devise, amount: Double): Double =
    amount * from.valueInDollar / to.valueInDollar

  def getFormattedAmount(operationType: OperationType, amount: Double): Double =
    operationType match {
      case Deposit    => amount
      case Withdrawal => -amount
    }

  def getNewFund(
      newFund: Double,
      devise: Devise,
      accountFund: Double,
      accountDevise: Devise,
      operationType: OperationType = Withdrawal
  ): Double = {
    val convertedDevise = getAmountInDevise(devise, accountDevise, newFund)
    operationType match {
      case Deposit    => accountFund + convertedDevise
      case Withdrawal => accountFund - convertedDevise
    }
  }

  def getCurrentAmount(
      accountDevise: Devise,
      transactions: Seq[Transaction]
  ): Double =
    transactions.foldRight(0.0) { (transaction, currentFund) =>
      {
        val amount          = round(getAmountInDevise(transaction.devise, accountDevise, transaction.amount))
        val formattedAmount = getFormattedAmount(transaction.operationType, amount)
        currentFund + formattedAmount
      }
    }

  def getFormattedTransactions(
      accountDevise: Devise,
      transactions: Seq[Transaction]
  ): List[TransactionResult] =
    transactions.foldRight(List[TransactionResult]()) { (transaction, trxResults) =>
      {
        val amount          = round(getAmountInDevise(transaction.devise, accountDevise, transaction.amount))
        val formattedAmount = getFormattedAmount(transaction.operationType, amount)
        val currentFund = trxResults.headOption match {
          case Some(lastComputedTrx) => lastComputedTrx.currentFund + formattedAmount
          case None                  => formattedAmount
        }

        TransactionResult(
          formattedAmount,
          accountDevise.name,
          currentFund,
          new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(transaction.date)
        ) :: trxResults
      }
    }
}
