package com.reviewranker

import akka.actor.typed.ActorSystem
import com.reviewranker.actor.Supervisor
import com.reviewranker.actor.Supervisor.Start

object ReviewRankerApp {
  def main(args: Array[String]): Unit = {
    val supervisor = ActorSystem(Supervisor(), "domainRanker")

    supervisor ! Start

    println("Press ENTER to terminate")
    scala.io.StdIn.readLine()
    supervisor.terminate()
  }
}