package models

import scala.language.postfixOps

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

/**
 * A Match consists of multiple Games, with a Game having two Winners and two Losers
 */
case class Game(id: Pk[Long], matchId: Long, winner1: String, winner2: String, loser1: String, loser2: String, score: Game.Score.Value) {
  require(!winner1.isEmpty() && !winner2.isEmpty() && !loser1.isEmpty() && !loser2.isEmpty())  
  require(winner1 != winner2 && winner1 != loser1 && winner1 != loser2)
  require(winner2 != loser1 && winner2 != loser2)
  require(loser1 != loser2)
  
  def players: Seq[String] = {
    List(winner1, winner2, loser1, loser2)
  }
  
  def playerScore(player: String): Int =
    if (winner1 == player || winner2 == player) 2 // Won 2-0 or 2-1
    else if (score == Game.Score.Two_One) 1       // Lost 2-1
    else 0                                        // Lost 2-0
}

object Game {
  
  object Score extends Enumeration {
    val Two_Zero = Value("2-0") 
    val Two_One  = Value("2-1")
  }
  
  // ===== Overloaded Apply =====
  
  // Allowing this is really ugly, but other than duplicating a portion of Game, I'm not sure how to avoid it
  // It is needed by the controllers to provide the services with the results of a Game, but at that stage the controllers don't have a match id
  def apply(winner1: String, winner2: String, loser1: String, loser2: String, score: Game.Score.Value): Game = {
    Game(NotAssigned, 0, winner1, winner2, loser1, loser2, score)
  }
      
  // ===== ResultSet Parsers =====
  
  val simple = {
    get[Pk[Long]] ("game.id") ~
    get[Long]     ("game.match_id") ~
    get[String]   ("game.winner1") ~
    get[String]   ("game.winner2") ~
    get[String]   ("game.loser1") ~
    get[String]   ("game.loser2") ~
    get[String]   ("game.result") map {
      case id ~ matchId ~ winner1 ~ winner2 ~ loser1 ~ loser2 ~ result => Game(id, matchId, winner1, winner2, loser1, loser2, Score.withName(result))
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
      'result     -> game.score.toString
    ).executeInsert().map(newId => game.copy(id = Id(newId))).get
  }
}