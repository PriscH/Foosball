package models

import scala.language.postfixOps

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class Game(id: Pk[Long], matchId: Long, winner1: String, winner2: String, loser1: String, loser2: String, result: String) {
  require(!winner1.isEmpty() && !winner2.isEmpty() && !loser1.isEmpty() && !loser2.isEmpty() && !result.isEmpty())
  require(winner1 != winner2 && winner1 != loser1 && winner1 != loser2)
  require(winner2 != loser1 && winner2 != loser2)
  require(loser1 != loser2)
  require(List("2-0", "2-1").contains(result))
}

object Game {
      
  // ===== ResultSet Parsers =====
  
  val simple = {
    get[Pk[Long]]   ("game.id") ~
    get[Long]   ("game.match_id") ~
    get[String] ("game.winner1") ~
    get[String] ("game.winner2") ~
    get[String] ("game.loser1") ~
    get[String] ("game.loser2") ~
    get[String] ("game.result")  map {
      case id ~ matchId ~ winner1 ~ winner2 ~ loser1 ~ loser2 ~ result => Game(id, matchId, winner1, winner2, loser1, loser2, result)
    }
  }
  
  // ===== Query Operations ======
  
  def all(): Seq[Game] = DB.withConnection { implicit connection =>
    SQL("select * from game").as(Game.simple *)
  }
  
  // ===== Persistance Operations =====

  def create(game: Game): Game = DB.withConnection { implicit connection =>
    SQL("insert into game (match_id, winner1, winner2, loser1, loser2, result) values ({matchId}, {winner1}, {winner2}, {loser1}, {loser2}, {result})").on(
      'matchId    -> game.matchId,
      'winner1    -> game.winner1,
      'winner2    -> game.winner2,
      'loser1     -> game.loser1,
      'loser2     -> game.loser2,
      'result     -> game.result
    ).executeInsert()
    
    return game
  }
}