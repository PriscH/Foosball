package services

import anorm.NotAssigned
import domain._
import models._
import org.joda.time.DateTime

import scala.collection.mutable

object MatchService {
  
  val RecentMatchCount = 3
  val ConflictWindowInMinutes = 60;

  // ===== Interface =====
  
  def findMatchWithGames(matchId: Long): Option[MatchWithGames] = {
    Match.findById(matchId) map { foosMatch => MatchWithGames(foosMatch, Game.findByMatch(foosMatch.id.get)) }
  }

  /**
   * Returns all recent matches played
   */
  def findAllRecentMatches(): Seq[MatchWithResults] = {
    Match.findRecent(RecentMatchCount).map(toMatchWithResults)
  }

  def findAllMatchesWithResults(): Seq[MatchWithResults] = {
    val foosMatches = Match.all()
    val matchResults = MatchResult.all()
    val matchResultsByMatch = matchResults.groupBy(_.matchId)

    foosMatches.map(foosMatch => MatchWithResults(foosMatch, matchResultsByMatch(foosMatch.id.get)))
  }

  def findAllMatchesWithGames(): Seq[MatchWithGames] = {
    val foosMatches = Match.all()
    val games = Game.all()
    val gamesByMatch = games.groupBy(_.matchId)

    foosMatches.map(foosMatch => MatchWithGames(foosMatch, gamesByMatch(foosMatch.id.get)))
  }

  /**
   * Returns the most recent matches captured in the system, and will always include at least the latest match for the player.
   */
  def findRecentMatchesForPlayer(player: String): Seq[MatchWithResults] = {
    val recentMatches = findAllRecentMatches()

    if (recentMatches.exists(_.players contains player)) recentMatches
    else recentMatches.take(RecentMatchCount - 1) ++ Match.findRecentForPlayer(player, 1).map(toMatchWithResults)
  }

  /**
   * Searches for a Match with the exact same Game results (ignoring order) within the last 60 minutes.
   * If more than one Match is found the most recent one will be returned.
   */
  // TODO: Unit test this (probably need to understand the cake pattern first in order to mock the Object calls)
  // TODO: This doesn't work correctly, it doesn't remove games that already match, so this will fail 5-2 5-1 5-0 vs 5-1 5-1 5-1
  def findRecentConflictingMatch(games: Seq[Game]): Option[Match] = {    
    val matches = Match.findCapturedSince(new DateTime().minusMinutes(ConflictWindowInMinutes))
    val gamesByMatch = Game.findByMatches(matches.map(_.id.get))
    
    matches.sortWith { 
      case (alpha, beta) => alpha.capturedDate.isAfter(beta.capturedDate)
    } find { foosMatch => 
      gamesByMatch(foosMatch.id.get).forall{ matchGame =>
        games.exists { paramGame => matchGame isSame paramGame }
      }
    }
  }
  
  def captureMatch(games: Seq[Game])(implicit user: User) {
    val foosMatch = Match.create(Match(NotAssigned, new DateTime(), user.name, Match.Format.CompleteMatch))
    val persistedGames = games.map(game => Game.create(game.copy(matchId = foosMatch.id.get))) // This is ugly :(
    val matchWithGames = MatchWithGames(foosMatch, persistedGames)

    val winCounts = matchWithGames.players.map(matchWithGames.gameWins(_))

    matchWithGames.players.map{player =>
      val playerWins = matchWithGames.gameWins(player)
      val playerResult = calculateResult(playerWins, winCounts)
      val playerRank = calculateRank(playerWins, winCounts)

      MatchResult.create(MatchResult(matchWithGames.matchId, player, playerResult, playerRank, playerWins, matchWithGames.goalDifference(player)))
    }

    EloService.updateElo(matchWithGames)
  }
  
  // ===== Helpers =====    
  
  private def toMatchWithResults(foosMatch: Match): MatchWithResults = MatchWithResults(foosMatch, MatchResult.findByMatch(foosMatch.id.get))

  private def calculateResult(playerWins: Int, winCounts: Iterable[Int]): MatchResult.Result = {
    if (winCounts.count(_ >= playerWins) == 1) MatchResult.Result.Winner
    else if (winCounts.count(_ <= playerWins) == 1) MatchResult.Result.Loser
    else MatchResult.Result.NoResult
  }

  private def calculateRank(playerWins: Int, winCounts: Iterable[Int]): Int = {
    // Rank falls by 1 for every player with more wins (same wins == same rank)
    winCounts.count(_ > playerWins) + 1
  }
}