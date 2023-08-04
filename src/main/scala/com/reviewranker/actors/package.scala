package com.reviewranker

package object actors {
  val SearchCategoryName = "store"
  val SearchCategoriesUrl = "https://www.trustpilot.com/api/consumersitesearch-api/categories/search?country=US&locale=en-US&query=%s"
  val SearchDomainsByCategoryUrl = "https://www.trustpilot.com/_next/data/categoriespages-consumersite-3698/categories/%s.json?sort=%s"
  val RecentReviewsByDomains = "https://www.trustpilot.com/api/categoriespages/%s/reviews"

}
