package models

import scala.language.postfixOps

import org.joda.time.DateTime

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import util.db.AnormExtension.rowToDateTime

case class MatchResult(matchId: Long, player: String, result: String, rank: Int, score: Int) {
  
  def outrightResult: Boolean = {result == "Winner" || result == "Loser"}
  
  def pseudoResult: Boolean = {result == "Pseudo-Winner" || result == "Pseudo-Loser"}
  
  def noResult: Boolean = {result == "Nothing"}
  
  def resultString: String = 
    if (noResult) player + " did not achieve a result"
    else player + " was the " + result
  
}

object MatchResult {
  
  // ===== ResultSet Parsers =====
  
  val simple = {
    get[Long]   ("match_result.match_id") ~
    get[String] ("match_result.player") ~
    get[String] ("match_result.result") ~
    get[Int]    ("match_result.rank") ~
    get[Int]    ("match_result.score") map {
      case matchId ~ player ~ result ~ rank ~ score => MatchResult(matchId, player, result, rank, score)
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
  
  def findConfirmed(): Seq[MatchResult] = DB.withConnection { implicit connection =>
    SQL("""
        select * from match_result inner join match on (match_result.match_id = match.id)
        where match.confirmed_by is not null
        """).as(MatchResult.simple *)
  }
  
  
  // ===== Persistance Operations =====

  def create(matchResult: MatchResult): MatchResult = DB.withConnection { implicit connection =>
    SQL("insert into match_result values ({matchId}, {player}, {result}, {rank}, {score})").on(
      'matchId   -> matchResult.matchId,
      'player    -> matchResult.player,
      'result    -> matchResult.result,
      'rank      -> matchResult.rank,
      'score     -> matchResult.score
    ).executeUpdate()
    
    return matchResult
  }
}