package com.sgib.kata.service

import com.sgib.kata.service.utils.Devise
import io.circe._
import io.circe.generic.semiauto._

package object models {

  import java.util.UUID

  case class DepositRequest(accountId: UUID, amount: Double, devise: Devise)
  case class WithdrawalRequest(accountId: UUID, amount: Double, devise: Devise)

  case class TransactionResult(amount: Double, devise: String, currentFund: Double, date: String)

  implicit val transactionResultEncoder: Encoder[TransactionResult] = deriveEncoder
  implicit val depositRequestDecoder: Decoder[DepositRequest]       = deriveDecoder
  implicit val withdrawalRequestDecoder: Decoder[WithdrawalRequest] = deriveDecoder
  implicit val depositRequestEncoder: Encoder[DepositRequest]       = deriveEncoder
  implicit val withdrawalRequestEncoder: Encoder[WithdrawalRequest] = deriveEncoder

}
