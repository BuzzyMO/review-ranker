package com.reviewranker.util.parser

object Json {
  def decode[T](json: String)(implicit p: JsonParser[T]): T = {
    val decoded = p.parse(json)

    decoded match {
      case Left(ex) =>
        throw new IllegalArgumentException(s"Invalid JSON object: ${ex.getMessage}")
      case Right(resp) => resp
    }
  }

}
