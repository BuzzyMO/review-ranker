package com.reviewranker.actors

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import com.reviewranker.entities.CategoryResponse
import com.reviewranker.entities.Category
import com.reviewranker.entities.Domain
import com.reviewranker.entities.DomainResponse
import com.reviewranker.util.parser.JsonParserInstances._
import com.reviewranker.util.parser.Json

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object Fetcher {
  sealed trait Command

  final case class FetchCategoriesByName(name: String, replyTo: ActorRef[CategoriesFetched]) extends Command

  final case class FetchDomainsByCategories(categories: List[Category], replyTo: ActorRef[DomainsFetched]) extends Command

  final case class FetchTrafficForDomains(domains: List[String], replyTo: ActorRef[TrafficFetched]) extends Command

  sealed trait Event

  final case class CategoriesFetched(categories: List[Category]) extends Event

  final case class DomainsFetched(domains: List[Domain]) extends Event

  final case class TrafficFetched(traffic: List[String]) extends Event

  private def pullCategories(name: String)(implicit system: ActorSystem[Nothing]): Future[List[Category]] = {
    implicit val execContext = system.executionContext
    val categoriesFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = s"$SearchCategoriesUrl".format(name)))

    categoriesFuture
      .flatMap(toStrictEntity)
      .map { strictEntity =>
        val json = strictEntity.data.utf8String
        val resp = Json.decode[CategoryResponse](json)

        resp.categories
      }
  }

  private def pullDomains(categories: List[Category])(implicit system: ActorSystem[Nothing]): Future[List[Domain]] = {
    implicit val execContext = system.executionContext
    val sortParam = "latest_review"

    val domainFutures = categories.map { c =>
      val domainsFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = s"$SearchDomainsByCategoryUrl".format(c.categoryId, sortParam)))
      domainsFuture
        .flatMap(toStrictEntity)
        .map { strictEntity =>
          val json = strictEntity.data.utf8String
          val resp = Json.decode[DomainResponse](json)

          resp.recentlyReviewedBusinessUnits
        }
    }

    Future.sequence(domainFutures).map(_.flatten)
  }

  private def toStrictEntity(resp: HttpResponse)(implicit system: ActorSystem[Nothing]): Future[HttpEntity.Strict] = {
    val timeout = 300.millis

    resp.entity.toStrict(timeout)
  }

  def apply(): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      implicit val system = context.system
      implicit val execContext = system.executionContext

      message match {
        case FetchCategoriesByName(name, replyTo) =>
          val categoriesFuture = pullCategories(name)

          categoriesFuture.map(replyTo ! CategoriesFetched(_))

          Behaviors.same
        case FetchDomainsByCategories(categories, replyTo) =>
          val domainsFuture = pullDomains(categories)

          domainsFuture.map(replyTo ! DomainsFetched(_))

          Behaviors.same
      }
    }
}
