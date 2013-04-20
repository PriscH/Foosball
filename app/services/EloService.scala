package services

import scala.math

import anorm.NotAssigned

import org.joda.time.DateTime

import models._
import domain.EloWithChange

object EloService {

  private val StartingElo = 1200.0
  private val StartingChange = 0.0
  private val EloWeight = 400.0
  private val KValue = 32.0

  // ===== Interface =====

  def findCurrentElos(players: Seq[String]): Map[String, Double] = {
    findCurrentElos(players, (elo: PlayerElo) => elo.elo, StartingElo)
  }

  def findCurrentElosWithChanges(players: Seq[String]): Map[String, EloWithChange] = {
    findCurrentElos(players, (elo: PlayerElo) => (elo.elo, elo.change), (StartingElo, StartingChange))
  }


  // Elo calculation based on http://sradack.blogspot.com/2008/06/elo-rating-system-multiple-players.html
  def updateElo(matchResults: Seq[MatchResult]) = {
    val players = matchResults.map(_.player)
    val currentElos = findCurrentElos(players)

    val totalMatchScore = matchResults.map(_.score).sum
    val updatedElos = currentElos.map(eloTuple => eloTuple._1 -> {
        val expectedScore = currentElos.filter(_._1 != eloTuple._1).values.map(opponentElo => estimateScoreVersus(eloTuple._2, opponentElo)).sum / 6.0
        val actualScore = matchResults.find(_.player == eloTuple._1).get.score.toDouble / totalMatchScore

        // As a win is only 0.5, we multiply the KValue by 2 in order to match the jumps expected from a game where a win is 1.0
        eloTuple._2 + 2 * KValue * (actualScore - expectedScore)
      }
    )

    val currentDate = new DateTime()
    matchResults.map(result => {
      val previousElo = currentElos.find(_._1 == result.player).get._2
      val updatedElo = updatedElos.find(_._1 == result.player).get._2

      PlayerElo.create(PlayerElo(NotAssigned, result.player, currentDate, result.matchId, (updatedElo - previousElo), updatedElo))
    })
  }

  // ===== Helper Methods =====

  private def findCurrentElos[V](players: Seq[String], valueMapping: (PlayerElo => V), default: V): Map[String, V] = {
    val currentElos = PlayerElo.findLatestElos()
    players.map(player => player -> {
      currentElos.find(_.player == player) match {
        case Some(elo) => valueMapping(elo)
        case None      => default
      }
    }).toMap
  }

  // Visible for testing
  private[services] def estimateScoreVersus(playerElo: Double, opponentElo: Double): Double = 1.0 / (1.0 + math.pow(10, ((opponentElo - playerElo) / EloWeight)))

}