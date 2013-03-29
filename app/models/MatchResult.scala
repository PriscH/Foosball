package models

import scala.language.postfixOps

import org.joda.time.DateTime

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import util.db.AnormExtension.rowToDateTime

case class MatchResult(matchId: Long, player: String, result: String, rank: Int)

object MatchResult {
  
  // ===== ResultSet Parsers =====
  
  val simple = {
    get[Long]   ("matchResult.matchId") ~
    get[String] ("matchResult.player") ~
    get[String] ("matchResult.result") ~
    get[Int]    ("matchResult.rank") map {
      case matchId ~ player ~ result ~ rank => MatchResult(matchId, player, result, rank)
    }
  }
  
  // ===== Query Operations ======
  
  def all(): Seq[MatchResult] = DB.withConnection { implicit connection =>
    SQL("select * from match_result").as(MatchResult.simple *)
  }
  
  // ===== Persistance Operations =====

  def create(matchResult: MatchResult): MatchResult = DB.withConnection { implicit connection =>
    SQL("insert into match_result values ({matchId}, {player}, {result}, {rank})").on(
      'matchId   -> matchResult.matchId,
      'player    -> matchResult.player,
      'result    -> matchResult.result,
      'rank      -> matchResult.rank
    ).executeUpdate()
    
    return matchResult
  }
}