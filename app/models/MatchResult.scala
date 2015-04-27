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
case class MatchResult(matchId: Long, player: String, result: MatchResult.Result, rank: Int, gameScore: Int, goalScore: Int) {
  
  // Did this player win or lose
  def hasResult: Boolean = { result == MatchResult.Result.Winner || result == MatchResult.Result.Loser }
  
  def resultString: String = result match {
    case MatchResult.Result.Winner       => player + " won"
    case MatchResult.Result.Loser        => player + " lost"
    case _                               => player + " did nothing special"
  }
}

object MatchResult {

  sealed abstract class Result(val name: String) {
    override def toString = name
  }

  object Result {
    case object Winner extends Result("Winner")
    case object Loser extends Result("Loser")
    case object NoResult extends Result("NoResult")

    val values = List(Winner, Loser, NoResult)
    def apply(name: String) = values.find(_.name == name).get
  }
  
  // ===== ResultSet Parsers =====
  
  val simple = {
    get[Long]   ("match_result.match_id") ~
    get[String] ("match_result.player") ~
    get[String] ("match_result.result") ~
    get[Int]    ("match_result.rank") ~
    get[Int]    ("match_result.game_score") ~
    get[Int]    ("match_result.goal_score") map {
      case matchId ~ player ~ result ~ rank ~ gameScore ~ goalScore => MatchResult(matchId, player, Result(result), rank, gameScore, goalScore)
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
    SQL("insert into match_result values ({matchId}, {player}, {result}, {rank}, {gameScore}, {goalScore})").on(
      'matchId   -> matchResult.matchId,
      'player    -> matchResult.player,
      'result    -> matchResult.result.toString,
      'rank      -> matchResult.rank,
      'gameScore -> matchResult.gameScore,
      'goalScore -> matchResult.goalScore
    ).executeInsert()
    
    return matchResult
  }
}