package com.sgib.kata.service.models

import io.circe.generic.extras.Configuration

object implicits {

  implicit val defaultCirceConfig: Configuration =
    Configuration.default.withSnakeCaseConstructorNames.withSnakeCaseMemberNames

}
