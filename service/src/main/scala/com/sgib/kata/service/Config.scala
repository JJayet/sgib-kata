package com.sgib.kata.service

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

object Config {
  case class Config(
      server: ServerConfig,
  )

  case class ServerConfig(
      host: String,
      port: Int
  )

  object Config {
    implicit val serverConfigReader: ConfigReader[ServerConfig] = deriveReader[ServerConfig]
    implicit val configReader: ConfigReader[Config]             = deriveReader[Config]
  }
}
