package services

import anorm.NotAssigned
import org.joda.time.DateTime
import models._

object MatchService {

  // ===== Interface =====
  
  // TODO: I don't want to pass the user through, but I have no idea how to retrieve it from the Session at this stage
  def captureMatch(games: Seq[Game], username: String) {
    val foosMatch = Match.create(Match(NotAssigned, new DateTime(), username, None, "Two-on-Two"))
    games.map(game => Game.create(Game(foosMatch.id.get, game)))
    
    val players = List(games(0).winner1, games(0).winner2, games(0).loser1, games(0).loser2)
    val playerScores = players.map(player => (player, playerScore(player, games))).toMap  
    val playerRanks = players.map(player => (player, playerRank(playerScores(player), playerScores.values))).toMap
    val playerResults = players.map(player => (player, playerResult(playerScores(player), playerScores.values))).toMap
    
    val matchResults = players.map(player => MatchResult(foosMatch.id.get, player, playerResults(player), playerRanks(player), playerScores(player)))
    matchResults.map(matchResult => MatchResult.create(matchResult))
  }
  
  // ===== Helpers =====
  
  private def playerScore(player: String, games: Seq[Game]): Int = {
    games.map(game =>
      if (game.winner1 == player || game.winner2 == player) 2
      else if (game.result == "2-1") 1
      else 0
    ).sum
  }
  
  private def playerRank( playerScore: Int, scores: Iterable[Int]): Int = {
    // Rank falls by 1 for every player with a higher score (same score == same rank) 
    scores.count(_ > playerScore) + 1
  }
  
  // Visible for testing
  private[services] def playerResult(playerScore: Int, scores: Iterable[Int]): String = playerScore match {
    case x if scores.count(_ == x) > 1                      => "Nothing"       // Always nothing if more than one person has this score
    case 6                                                  => "Winner"        // Outright winner always has exactly 6 points
    case x if scores.count(_ < x) >= 2                      => "Pseudo-Winner" // Not an outright winner, but has a better score than at least two other players
    case x if (scores.count(_ > x) == 3 && scores.max != 6) => "Loser"         // All three other players have a better score, but none of them are an outright winner
    case x if (scores.count(_ > x) >= 2)                    => "Pseudo-Loser"  // Not an outright loser, but has a worst score than at least two other players
  }
}