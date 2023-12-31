package com.reviewranker.actor

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.Cookie
import akka.http.scaladsl.model.headers.HttpCookiePair
import com.reviewranker.dto.DomainDto
import com.reviewranker.entity.{Domain, DomainResponse}
import com.reviewranker.util.parser.JsonParserInstances._
import com.reviewranker.util.parser.Json
import org.jsoup.Jsoup

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object Fetcher {
  val MonthlyVisitsID = "MONTHLY_VISITS"

  sealed trait Command

  final case class FetchDomainsByCategories(categoryIds: List[String], replyTo: ActorRef[DomainsFetched]) extends Command

  final case class FetchTrafficForDomains(domains: List[Domain], replyTo: ActorRef[TrafficFetched]) extends Command

  sealed trait Event

  final case class DomainsFetched(domains: List[Domain]) extends Event

  final case class TrafficFetched(domains: List[DomainDto]) extends Event

  final case class Timeout() extends Event

  private def pullDomains(categoryIds: List[String])(implicit system: ActorSystem[Nothing]): Future[List[Domain]] = {
    implicit val execContext = system.executionContext

    val domainFutures = categoryIds.map(pullDomains)

    Future.sequence(domainFutures).map(_.flatten)
  }

  private def pullDomains(categoryId: String)(implicit system: ActorSystem[Nothing]): Future[List[Domain]] = {
    implicit val execContext = system.executionContext
    val sortParam = "latest_review"
    val domainsFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = s"$SearchDomainsByCategoryUrl".format(categoryId, sortParam)))

    domainsFuture
      .flatMap(toStrictEntity)
      .map { strictEntity =>
        val json = strictEntity.data.utf8String
        val resp = Json.decode[DomainResponse](json)

        resp.recentlyReviewedBusinessUnits
      }
  }

  private def pullTraffic(domains: List[Domain])(implicit system: ActorSystem[Nothing]): Future[List[DomainDto]] = {
    implicit val execContext = system.executionContext

    val trafficFutures = domains.map { d =>
      val trafficFuture = pullTraffic(d)

      trafficFuture.map(t => DomainDto(d, traffic = t))
    }

    Future.sequence(trafficFutures)
  }

  private def pullTraffic(domain: Domain)(implicit system: ActorSystem[Nothing]): Future[Int] = {
    implicit val execContext = system.executionContext
    val sessionCookie = HttpCookiePair(TrafficSessionName, TrafficSessionCookie)
    val reqHeaders = Seq(Cookie(sessionCookie))
    val visitAttr = "data-smvisits"
    val trafficFuture: Future[HttpResponse] = Http().singleRequest(
      HttpRequest(
        uri = s"$TrafficUrl".format(domain.identifyingName),
        headers = reqHeaders)
    )

    trafficFuture
      .flatMap(toStrictEntity)
      .map { strictEntity =>
        val html = strictEntity.data.utf8String
        val doc = Jsoup.parse(html)

        val mVisitsEl = doc.getElementById(MonthlyVisitsID)

        mVisitsEl.attributes().get(visitAttr).toInt
      }
  }

  private def toStrictEntity(resp: HttpResponse)(implicit system: ActorSystem[Nothing]): Future[HttpEntity.Strict] = {
    val timeout = 300.millis

    resp.entity.toStrict(timeout)
  }

  def apply(): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      implicit val system = context.system
      implicit val execContext = system.executionContext
      val log = context.system.log

      message match {
        case FetchDomainsByCategories(categories, replyTo) =>
          log.info("Fetch domains started")

          val domainsFuture = pullDomains(categories)

          domainsFuture.map(replyTo ! DomainsFetched(_))

          Behaviors.same
        case FetchTrafficForDomains(domains, replyTo) =>
          log.info("Fetch traffic started")

          val trafficFuture = pullTraffic(domains)

          trafficFuture.map(replyTo ! TrafficFetched(_))

          Behaviors.same
      }
    }
}