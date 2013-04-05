package services

import domain._
import models._

object RankingService {

  def loadCurrentRankings(): Seq[PlayerRanking] = {    
    val players = User.all()
    val matchResults = MatchResult.findConfirmed()
    
    val playerElos = players.map(player => {
      (player.name,
      PlayerElo.findLatestElos().find(_.player == player.name) match {
        case Some(elo) => elo.elo
        case None      => EloService.StartingElo
      })
    }).toMap
    
    players.map(player => {
      val playerElo = playerElos(player.name)      
      val playerResults = matchResults.filter(_.player == player.name)
      
      PlayerRanking(player.name,
                    playerElos.count(_._2 > playerElo) + 1,
                    playerResults.size,
                    playerResults.count(_.result == "Winner"),
                    playerResults.count(_.result == "Pseudo-Winner"),
                    playerResults.count(_.result == "Pseudo-Loser"),
                    playerResults.count(_.result == "Loser"),
                    playerElo)  
    })
  }
  
}