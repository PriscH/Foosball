package models

import scala.language.postfixOps

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class Game(id: Long, matchId: Long, winner1: String, winner2: String, loser1: String, loser2: String, winScore: Int, loseScore: Int)

object Game {
  
  // ===== ResultSet Parsers =====
  
  val simple = {
    get[Long]   ("game.id") ~
    get[Long]   ("game.matchId") ~
    get[String] ("game.winner1") ~
    get[String] ("game.winner2") ~
    get[String] ("game.loser1") ~
    get[String] ("game.loser2") ~
    get[Int]    ("game.winScore") ~
    get[Int]    ("game.loseScore") map {
      case id ~ matchId ~ winner1 ~ winner2 ~ loser1 ~ loser2 ~ winScore ~ loseScore => Game(id, matchId, winner1, winner2, loser1, loser2, winScore, loseScore)
    }
  }
  
  // ===== Query Operations ======
  
  def all(): Seq[Game] = DB.withConnection { implicit connection =>
    SQL("select * from game").as(Game.simple *)
  }
  
  // ===== Persistance Operations =====

  def create(game: Game): Game = DB.withConnection { implicit connection =>
    SQL("insert into game values ({id}, {matchId}, {winner1}, {winner2}, {loser1}, {loser2}, {winScore}, {loseScore})").on(
      'id         -> game.id,
      'matchId    -> game.matchId,
      'winner1    -> game.winner1,
      'winner2    -> game.winner2,
      'loser1     -> game.loser1,
      'loser2     -> game.loser2,
      'winScore   -> game.winScore,
      'loseScore  -> game.loseScore
    ).executeUpdate()
    
    return game
  }
}