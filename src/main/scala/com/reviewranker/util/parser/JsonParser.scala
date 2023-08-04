package com.reviewranker.util.parser

import io.circe.Error

trait JsonParser[T] {
  def parse(json: String): Either[Error, T]
}
