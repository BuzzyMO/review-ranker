package com.reviewranker.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.reviewranker.actor.Fetcher._

object DomainRanker {

  def apply(): Behavior[Fetcher.Event] = {
    Behaviors.setup({ context =>
      val f = context.spawn(Fetcher(), "fetcher")
      val categoryIds = JewelryStoreId :: ElectronicsStoreId :: FurnitureStoreId :: Nil
      f ! FetchDomainsByCategories(categoryIds, context.self)

      Behaviors.receiveMessage {
        case DomainsFetched(domains) =>
          val limitedDomains = domains.take(DomainsLimit)

          f ! FetchTrafficForDomains(limitedDomains, context.self)

          Behaviors.same
        case TrafficFetched(domains) =>
          domains
            .sortBy(d => (d.domain.numberOfReviews, d.traffic))(Ordering[(Int, Int)].reverse)
            .foreach(d => println(s"${d.domain.identifyingName} - Total reviews: ${d.domain.numberOfReviews} - " +
              s"Traffic: ${d.traffic} - Last Review: ${d.domain.review.text}"))

          Behaviors.withTimers { timers =>
            timers.startTimerWithFixedDelay(Timeout(), UpdateDataDelay)

            Behaviors.same
          }
        case Timeout() =>
          f ! FetchDomainsByCategories(categoryIds, context.self)

          Behaviors.same
      }
    })
  }
}
