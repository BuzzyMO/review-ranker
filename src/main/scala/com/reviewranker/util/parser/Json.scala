package com.reviewranker.util.parser

import akka.actor.typed.ActorSystem

object Json {
  def decode[T](json: String)(implicit p: JsonParser[T], system: ActorSystem[Nothing]): T = {
    val decoded = p.parse(json)

    decoded match {
      case Left(ex) =>
        val exMsg = s"Invalid JSON object: ${ex.getMessage}"

        system.log.error(exMsg)
        throw new IllegalArgumentException(exMsg)
      case Right(resp) => resp
    }
  }

}
