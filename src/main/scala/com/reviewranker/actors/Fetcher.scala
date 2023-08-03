package com.reviewranker.actors

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.stream.Materializer
import com.reviewranker.entities.CategoriesResponse
import com.reviewranker.entities.Category
import io.circe.Decoder
import io.circe.Error
import io.circe.generic.auto._
import io.circe.generic.semiauto.deriveDecoder
import io.circe.jawn.decode

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object Fetcher {
  sealed trait Command

  final case class FetchCategoriesByName(name: String, replyTo: ActorRef[CategoriesFetched]) extends Command

  final case class FetchDomainsByCategories(categories: List[Category], replyTo: ActorRef[DomainsFetched]) extends Command

  final case class FetchTrafficForDomains(domains: List[String], replyTo: ActorRef[TrafficFetched]) extends Command

  sealed trait Event

  final case class CategoriesFetched(categories: List[Category]) extends Event

  final case class DomainsFetched(domains: List[String]) extends Event

  final case class TrafficFetched(traffic: List[String]) extends Event

  private def pullCategories(name: String)(implicit system: ActorSystem[Nothing]): Future[HttpResponse] = {
    Http().singleRequest(HttpRequest(uri = SearchCategoriesUrl + name))
  }

  def apply(): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match {
        case FetchCategoriesByName(name, replyTo) =>
          implicit val system = context.system
          implicit val execContext = system.executionContext
          implicit val materializer: Materializer = Materializer(system)
          val categoriesFuture = pullCategories(name)
          categoriesFuture.flatMap { response =>
            val timeout = 300.millis
            response.entity.toStrict(timeout)
          }.map { strictEntity =>
            implicit val categoriesResponseDecoder: Decoder[CategoriesResponse] = deriveDecoder[CategoriesResponse]
            val body = strictEntity.data.utf8String

            val decoded: Either[Error, CategoriesResponse] = decode[CategoriesResponse](body)

            decoded match {
              case Left(ex) =>
                throw new IllegalArgumentException(s"Invalid JSON object: ${ex.getMessage}")
              case Right(categoriesResp) =>
                replyTo ! CategoriesFetched(categoriesResp.categories)
            }
          }
          Behaviors.same
      }

    }
}
