package com.reviewranker.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.reviewranker.actor.Fetcher.CategoriesFetched
import com.reviewranker.actor.Fetcher.DomainsFetched
import com.reviewranker.actor.Fetcher.TrafficFetched
import com.reviewranker.actor.Fetcher.FetchCategoriesByName
import com.reviewranker.actor.Fetcher.FetchDomainsByCategories
import com.reviewranker.actor.Fetcher.FetchTrafficForDomains

object DomainRanker {

  def apply(): Behavior[Fetcher.Event] = {
    Behaviors.setup({ context =>
      val f = context.spawn(Fetcher(), "fetcher")
      f ! FetchCategoriesByName(SearchCategoryName, context.self)

      Behaviors.receiveMessage { case CategoriesFetched(categories) =>
        f ! FetchDomainsByCategories(categories, context.self)

        Behaviors.receiveMessage { case DomainsFetched(domains) =>

          f ! FetchTrafficForDomains(domains, context.self)

          Behaviors.receiveMessage { case TrafficFetched(domains) =>
            domains
              .sortBy(d => (d.domain.numberOfReviews, d.traffic))
              .foreach(d => println(s"${d.domain.identifyingName} - ${d.domain.numberOfReviews} - ${d.traffic} - ${d.domain.review.text}"))

            apply()
          }

        }
      }
    })
  }
}
