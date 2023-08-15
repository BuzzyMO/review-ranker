package com.reviewranker.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.reviewranker.actor.Fetcher._

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object DomainRanker {

  def apply(): Behavior[Fetcher.Event] = {
    Behaviors.setup({ context =>
      val f = context.spawn(Fetcher(), "fetcher")
      f ! FetchCategoriesByName(SearchCategoryName, context.self)

      Behaviors.receiveMessage {
        case CategoriesFetched(categories) =>
          f ! FetchDomainsByCategories(categories, context.self)

          Behaviors.same
        case DomainsFetched(domains) =>
          f ! FetchTrafficForDomains(domains, context.self)

          Behaviors.same
        case TrafficFetched(domains) =>
          domains
            .sortBy(d => (d.domain.numberOfReviews, d.traffic))
            .foreach(d => println(s"${d.domain.identifyingName} - ${d.domain.numberOfReviews} - ${d.traffic} - ${d.domain.review.text}"))

          Behaviors.withTimers { timers =>
            timers.startTimerWithFixedDelay(Timeout(), 5 seconds)

            Behaviors.same
          }
        case Timeout() =>
          f ! FetchCategoriesByName(SearchCategoryName, context.self)

          Behaviors.same
      }
    })
  }
}
