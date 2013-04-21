package models

import scala.language.postfixOps

import org.joda.time.DateTime

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

/**
 * Stores the outcome of a match per player including the actual result, rank and score.
 */
case class MatchResult(matchId: Long, player: String, result: MatchResult.Result.Value, rank: Int, score: Int) {
  
  // Did this player win or lose outright
  def outrightResult: Boolean = { result == MatchResult.Result.Winner || result == MatchResult.Result.Loser }
  
  // Did this player achieve a pseudo win or loss
  def pseudoResult: Boolean = { result == MatchResult.Result.PseudoWinner || result == MatchResult.Result.PseudoLoser }
  
  // Was this player on par with another player, i.e. no result
  def noResult: Boolean = {result == MatchResult.Result.NoResult }
  
  def resultString: String = result match {
    case MatchResult.Result.Winner       => player + " won"
    case MatchResult.Result.Loser        => player + " lost"
    case MatchResult.Result.PseudoWinner => player + " was the pseudo-winner"
    case MatchResult.Result.PseudoLoser  => player + " was the pseudo-loser"
    case _                               => player + " did nothing special"
  }
}

object MatchResult {
  
  object Result extends Enumeration {
    val Winner       = Value("Winner")
    val PseudoWinner = Value("Pseudo-Winner")
    val PseudoLoser  = Value("Pseudo-Loser")
    val Loser        = Value("Loser")
    val NoResult     = Value("Nothing")
  }
  
  // ===== ResultSet Parsers =====
  
  val simple = {
    get[Long]   ("match_result.match_id") ~
    get[String] ("match_result.player") ~
    get[String] ("match_result.result") ~
    get[Int]    ("match_result.rank") ~
    get[Int]    ("match_result.score") map {
      case matchId ~ player ~ result ~ rank ~ score => MatchResult(matchId, player, Result.withName(result), rank, score)
    }
  }
  
  // ===== Query Operations ======
  
  def all(): Seq[MatchResult] = DB.withConnection { implicit connection =>
    SQL("select * from match_result").as(MatchResult.simple *)
  }
  
  def findByMatch(matchId: Long): Seq[MatchResult] = DB.withConnection { implicit connection =>
    SQL("select * from match_result where match_id = {matchId}").on(
        'matchId -> matchId
      ).as(MatchResult.simple *)
  }  
  
  // ===== Persistance Operations =====

  def create(matchResult: MatchResult): MatchResult = DB.withConnection { implicit connection =>
    SQL("insert into match_result values ({matchId}, {player}, {result}, {rank}, {score})").on(
      'matchId   -> matchResult.matchId,
      'player    -> matchResult.player,
      'result    -> matchResult.result.toString,
      'rank      -> matchResult.rank,
      'score     -> matchResult.score
    ).executeInsert()
    
    return matchResult;
  }
}