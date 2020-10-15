package com.sgib.kata.service.utils

import java.time.{LocalDate, ZoneId}
import java.util.{Date, UUID}

import io.circe.{Decoder, Encoder}
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert

abstract class OperationType(val name: String)
case object Deposit    extends OperationType("Deposit")
case object Withdrawal extends OperationType("withdrawal")

abstract class Devise(val name: String, val valueInDollar: Double)
case object Euro   extends Devise("euro", 1.18)
case object Dollar extends Devise("dollar", 1)
case object Yen    extends Devise("yen", 0.0095)

object Devise {

  val all: Map[String, Devise]             = Map(Euro.name -> Euro, Dollar.name -> Dollar, Yen.name -> Yen)
  def fromKey(key: String): Option[Devise] = all.get(key)

  implicit val encoder: Encoder[Devise] =
    Encoder.encodeString.contramap(_.name)

  implicit val decoder: Decoder[Devise] =
    Decoder.decodeString.emap(fromKey(_).toRight("Could not decode as devise"))

  implicit val configReader: ConfigReader[Devise] =
    ConfigReader.fromString(str => fromKey(str).toRight(CannotConvert(str, "Devise", "unknown")))
}

case class Account(accountId: UUID, allowedNegative: Double, devise: Devise)
case class Transaction(
    accountId: UUID,
    date: Date,
    devise: Devise,
    amount: Double,
    operationType: OperationType
)
case class DB(accounts: Seq[Account], devises: Seq[Devise], var transactions: Seq[Transaction])

object DB {
  val accountId: UUID = UUID.fromString("03f1cfeb-676f-47b2-bd2d-fae6be0520a3")
  private val accounts = Seq(
    Account(
      accountId = accountId,
      allowedNegative = 200,
      devise = Euro
    ),
    Account(
      accountId = UUID.fromString("03f1cfeb-676f-47b2-bd2d-fae6be0520a4"),
      allowedNegative = 0,
      devise = Euro
    )
  )

  private val transactions = Seq(
    Transaction(
      accountId = accountId,
      date = Date.from(LocalDate.now.minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant),
      devise = Euro,
      amount = 123,
      operationType = Deposit
    ),
    Transaction(
      accountId = accountId,
      date = Date.from(LocalDate.now.atStartOfDay(ZoneId.systemDefault()).toInstant),
      devise = Euro,
      amount = 23,
      operationType = Withdrawal
    )
  )

  val db: DB = DB(accounts, Seq(Euro, Dollar, Yen), transactions)
}
