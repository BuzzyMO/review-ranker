package com.reviewranker

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps

package object actor {
  val SearchCategoryName = "store"
  val SearchCategoriesUrl = "https://www.trustpilot.com/api/consumersitesearch-api/categories/search?country=US&locale=en-US&query=%s"
  val ConsumerSiteId = 3711
  val SearchDomainsByCategoryUrl = s"https://www.trustpilot.com/_next/data/categoriespages-consumersite-$ConsumerSiteId/categories/%s.json?sort=%s"
  val TrafficUrl = "https://web.vstat.info/%s"
  val TrafficSessionName = "vstat_session"
  val TrafficSessionCookie = "ErJt7YU24evVK9RUNNqcn95FhC5yai0jHAHmnVwN"
  val UpdateDataDelay: FiniteDuration = 5 minutes
  val DomainsLimit = 10
}
