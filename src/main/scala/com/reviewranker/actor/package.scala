package com.reviewranker

package object actor {
  val SearchCategoryName = "store"
  val SearchCategoriesUrl = "https://www.trustpilot.com/api/consumersitesearch-api/categories/search?country=US&locale=en-US&query=%s"
  val SearchDomainsByCategoryUrl = "https://www.trustpilot.com/_next/data/categoriespages-consumersite-3703/categories/%s.json?sort=%s"
  val TrafficUrl = "https://vstat.info/%s"
  val TrafficSessionName = "vstat_session"
  val TrafficSessionCookie = "ErJt7YU24evVK9RUNNqcn95FhC5yai0jHAHmnVwN"
}
