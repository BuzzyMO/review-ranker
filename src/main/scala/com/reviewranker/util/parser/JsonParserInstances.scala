package com.reviewranker.util.parser

import com.reviewranker.entity.DomainResponse
import io.circe.Decoder
import io.circe.Json
import io.circe.generic.auto._
import io.circe.generic.semiauto.deriveDecoder
import io.circe.parser

object JsonParserInstances {
  implicit val domainParser: JsonParser[DomainResponse] =
    (json: String) => {
      implicit val domainResponseDecoder: Decoder[DomainResponse] = deriveDecoder[DomainResponse]

      val cursor = parser.parse(json)
        .getOrElse(Json.Null)
        .hcursor
      cursor
        .downField("pageProps")
        .as[DomainResponse]
    }
}
