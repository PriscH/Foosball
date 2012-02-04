package controllers

import play.mvc.Controller
import util.security.Secure
import collection.mutable._

object Result extends Controller with Secure {

  def recordQuick = {
    var winner: Option[String] = None
    var loser: Option[String] = None
    val winnerId = params.get("winner")
    val loserId = params.get("loser")

    val playersListBuffer = ListBuffer.empty[String]
    for(index <- 1 to 4) {
      val playerId = "player" + index
      val playerName = params.get("player" + index)
      playersListBuffer.append(playerName)

      playerId match {
        case `winnerId` =>  winner = Some(playerName)
        case `loserId` =>   loser = Some(playerName)
        case _ =>
      }
    }

    val players = playersListBuffer.toList

    println(players)
    println(winner)
    println(loser)
  }

}