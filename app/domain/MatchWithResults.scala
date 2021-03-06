package domain

import org.joda.time._
import models._
import util.time.JodaExtension

/**
 * A Match along with the MatchResults for all its players
 */
case class MatchWithResults(foosMatch: Match, matchResults: Seq[MatchResult]) {
    
  def matchId: Long = foosMatch.id.get
  
  def players: Seq[String] = matchResults.map(_.player)

  def winner: Option[String] = playerWithResult(MatchResult.Result.Winner)

  def loser: Option[String] = playerWithResult(MatchResult.Result.Loser)

  def playerWithResult(result: MatchResult.Result): Option[String] = matchResults.find(_.result == result).map(_.player)
  
  def resultString(): String = {
    val winner = matchResults.find(_.result == MatchResult.Result.Winner)
    val loser = matchResults.find(_.result == MatchResult.Result.Loser)

    val resultString = if (winner.isDefined) {
      val winString = winner.get.player + " won"
      val loseString = if (loser.isDefined)
                          " and " + loser.get.player + " lost"
                       else
                          ""
      winString + loseString + " in a match with " + matchResults.filter(!_.hasResult).map(_.player).mkString(", ")
    } else if (loser.isDefined) {
      loser.get.player + " lost in a match with " + matchResults.filter(!_.hasResult).map(_.player).mkString(", ")
    } else {
      matchResults.map(_.player).mkString(", ") + " played a match which ended in a draw"
    }

    val period = new Period(foosMatch.capturedDate, new DateTime)
    resultString + " " + JodaExtension.formatElapsedTime(period)
  }  
}