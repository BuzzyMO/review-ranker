package com.reviewranker.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.reviewranker.actor.Fetcher._
import com.reviewranker.dto.DomainDto

object DomainRanker {
  def apply(): Behavior[Fetcher.Event] = setUp()

  def setUp(): Behavior[Fetcher.Event] = {
    Behaviors.setup({ context =>
      val f = context.spawn(Fetcher(), "fetcher")
      val categoryIds = JewelryStoreId :: ElectronicsStoreId :: FurnitureStoreId :: Nil

      f ! FetchDomainsByCategories(categoryIds, context.self)

      fetchData(Nil, categoryIds, f)
    })
  }

  private def fetchData(prevDomains: List[DomainDto], categoryIds: List[String], fetcher: ActorRef[Fetcher.Command]): Behavior[Fetcher.Event] = {
    Behaviors.receive { (context, msg) =>
      val log = context.log

      msg match {
        case DomainsFetched(domains) =>
          log.info("Domains have been fetched")

          val limitedDomains = domains.take(DomainsLimit)

          fetcher ! FetchTrafficForDomains(limitedDomains, context.self)

          Behaviors.same
        case TrafficFetched(domains) =>
          log.info("Traffic has been fetched")

          val updDomains = countRecentReviews(domains, prevDomains)

          updDomains
            .sortBy(d => (d.recentReviews, d.traffic))(Ordering[(Int, Int)].reverse)
            .foreach(printResultDomain)

          Behaviors.withTimers { timers =>
            timers.startTimerWithFixedDelay(Timeout(), UpdateDataDelay)

            log.info(s"Timer has been started, service will fetch new data in $UpdateDataDelay")

            fetchData(updDomains, categoryIds, fetcher)
          }
        case Timeout() =>
          log.info("Fetching new data...")

          fetcher ! FetchDomainsByCategories(categoryIds, context.self)

          Behaviors.same
      }
    }
  }

  private def countRecentReviews(domains: List[DomainDto], prevDomains: List[DomainDto]): List[DomainDto] = {
    domains.map { current =>
      val prevDomain = prevDomains.find(_.domain.identifyingName == current.domain.identifyingName)
      val nextRecentReviewsOpt = prevDomain.map { previous =>
        val diffReviews = current.domain.numberOfReviews - previous.domain.numberOfReviews

        previous.recentReviews + diffReviews
      }

      nextRecentReviewsOpt match {
        case Some(nRecentReviews) =>
          current.copy(recentReviews = nRecentReviews)
        case None => current
      }
    }
  }

  private def printResultDomain(d: DomainDto): Unit = {
    println(s"${d.domain.identifyingName} - Total reviews: ${d.domain.numberOfReviews} - " +
      s"Recent Review count: ${d.recentReviews} - Traffic: ${d.traffic} - Last Review: ${d.domain.review.text}")
  }
}
