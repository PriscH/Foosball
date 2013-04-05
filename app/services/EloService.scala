package services

import scala.math

import anorm.NotAssigned

import org.joda.time.DateTime

import models._

object EloService {
  
  val StartingElo = 100
  
  private val RatingAdvantage = 80
  private val KValue = 18
  
  // ===== Interface =====
  
  def updateElo(matchResults: Seq[MatchResult]) = {
    val players = matchResults.map(_.player)
    val currentElos = PlayerElo.findLatestElos().filter(playerElo => players.contains(playerElo.player))
    val playerElos = players.map(player => (player, currentElos.find(playerElo => playerElo.player == player) match {
      case Some(elo) => elo.elo
      case None      => StartingElo
    }))
    
    val totalMatchScore = matchResults.map(_.score).sum
    val updatedElos = playerElos.map(eloTuple => 
      (eloTuple._1, {
        val opponentElo = playerElos.filter(_._1 != eloTuple._1).map(_._2).sum / 3
        val expectedScore = 1.0 / (1 + math.pow(10.0, (opponentElo - eloTuple._2) / RatingAdvantage))
        val actualScore = matchResults.find(_.player == eloTuple._1).get.score.toDouble / totalMatchScore * 2
      
        math.round(eloTuple._2 + KValue * (actualScore - expectedScore)).toInt
      })
    )
    
    val currentDate = new DateTime()
    val modelElos = matchResults.map(result => {
      val previousElo = playerElos.find(_._1 == result.player).get._2
      val updatedElo = updatedElos.find(_._1 == result.player).get._2
      PlayerElo(NotAssigned, result.player, currentDate, result.matchId, (updatedElo - previousElo), updatedElo)
    })
    
    modelElos.map(PlayerElo.create)
  }

}