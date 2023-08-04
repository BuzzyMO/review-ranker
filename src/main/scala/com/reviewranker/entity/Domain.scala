package com.reviewranker.entity

case class Domain(businessUnitId: String,
                  displayName: String,
                  numberOfReviews: Int,
                  stars: Float,
                  trustScore: Float,
                  identifyingName: String,
                  categories: List[Category],
                  review: Review)
