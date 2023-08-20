package com.reviewranker.dto

import com.reviewranker.entity.Domain

case class DomainDto(domain: Domain, recentReviews: Int = 0, traffic: Int)
