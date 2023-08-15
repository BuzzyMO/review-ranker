package com.reviewranker.actor

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Supervisor {
  sealed trait Command

  case object Start extends Command

  def apply(): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match {
        case Start =>
          context.spawn(DomainRanker(), "domainRanker")

          Behaviors.same
      }
    }
}