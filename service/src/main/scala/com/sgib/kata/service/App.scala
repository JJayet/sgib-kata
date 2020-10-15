package com.sgib.kata.service

import cats.effect.{ExitCode, IO, IOApp}
import com.sgib.kata.service.logging.IOLogging
import com.sgib.kata.service.Config.Config
import com.sgib.kata.service.routes._
import com.sgib.kata.service.utils.DB
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import pureconfig.ConfigSource

object App extends IOApp with IOLogging {

  def run(args: List[String]): IO[ExitCode] = {
    val conf = ConfigSource.default.loadOrThrow[Config]

    log.info("Instantiating routes")

    val httpRoutes = Router[IO](
      "accounts" -> AccountController.routes(DB.db)
    ).orNotFound

    log.info("Instantiating server")

    BlazeServerBuilder[IO]
      .bindHttp(conf.server.port, conf.server.host)
      .withHttpApp(httpRoutes)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
