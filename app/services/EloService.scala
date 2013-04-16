package services

import scala.math

import anorm.NotAssigned

import org.joda.time.DateTime

import models._

object EloService {

  private val StartingElo = 100
  private val StartingChange = 0
  private val RatingAdvantage = 80
  private val KValue = 18

  // ===== Interface =====

  def findCurrentElo[B](players: Seq[String], valueMapping: (PlayerElo => B), default: B): Map[String, B] = {
    val currentElos = PlayerElo.findLatestElos()
    players.map(player => player -> {
      currentElos.find(_.player == player) match {
        case Some(elo) => valueMapping(elo)
        case None      => default
      }
    }).toMap
  }

  def findCurrentElos(players: Seq[String]): Map[String, Int] = {
    findCurrentElo(players, (elo: PlayerElo) => elo.elo, StartingElo)
  }

  def findCurrentElosWithChanges(players: Seq[String]): Map[String, (Int, Int)] = {
    findCurrentElo(players, (elo: PlayerElo) => (elo.elo, elo.change), (StartingElo, StartingChange))
  }

  def updateElo(matchResults: Seq[MatchResult]) = {
    val players = matchResults.map(_.player)
    val currentElos = findCurrentElos(players)

    val totalMatchScore = matchResults.map(_.score).sum
    val updatedElos = currentElos.map(eloTuple => eloTuple._1 -> {
        val averageOpponentElo = (currentElos.values.sum - eloTuple._2) / 3
        val expectedScore = 1.0 / (1 + math.pow(10.0, (averageOpponentElo - eloTuple._2) / RatingAdvantage))
        val actualScore = matchResults.find(_.player == eloTuple._1).get.score.toDouble / totalMatchScore * 2

        math.round(eloTuple._2 + KValue * (actualScore - expectedScore)).toInt
      }
    )

    val currentDate = new DateTime()
    matchResults.map(result => {
      val previousElo = currentElos.find(_._1 == result.player).get._2
      val updatedElo = updatedElos.find(_._1 == result.player).get._2
      PlayerElo.create(PlayerElo(NotAssigned, result.player, currentDate, result.matchId, (updatedElo - previousElo), updatedElo))
    })
  }

}