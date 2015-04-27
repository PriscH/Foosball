package domain

import models._
import play.api.db.DB
import anorm._
import org.joda.time.DateTime

/**
 * A Match along with all its Games
 */
case class MatchWithGames(foosMatch: Match, games: Seq[Game]) {

  def matchId = foosMatch.id.get

  def players = games.head.players

  def format = foosMatch.format

  def capturedDate = foosMatch.capturedDate

  def capturedBy = foosMatch.capturedBy

  def gameWins(player: String): Int = games.count(_.winners.contains(player))

  def goalDifference(player: String): Int = games.map(_.goalDifference(player)).sum
}

object MatchWithGames {

  // ===== Query Operations ======

  def all(): Seq[MatchWithGames] = {
    val matches = Match.all
    val gamesByMatch = Game.all.groupBy(_.matchId)

    matches.map{foosMatch => MatchWithGames(foosMatch, gamesByMatch(foosMatch.id.get))}
  }
}