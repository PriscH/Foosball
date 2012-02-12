package controllers

import play.mvc.Controller
import util.security.Secure
import collection.mutable._
import services.ResultService
import models._

object Result extends Controller with Secure {

  def recordQuick = {
    val winnerId = params.get("winner")
    val loserId = params.get("loser")

    val playerResultsBuffer = ListBuffer.empty[(String, Option[ResultType])]
    for(index <- 1 to 4) {
      val playerId = "player" + index
      val playerName = params.get("player" + index)

      playerId match {
        case `winnerId` => playerResultsBuffer.append((playerName, Some(WIN)))
        case `loserId`  => playerResultsBuffer.append((playerName, Some(LOSS)))
        case _          => playerResultsBuffer.append((playerName, None))
      }
    }
    val playerResults = playerResultsBuffer.toList

    ResultService.recordQuick(playerResults)
  }

}