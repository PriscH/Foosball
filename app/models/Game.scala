package models

import scala.language.postfixOps
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import scala.collection.mutable.MultiMap

/**
 * A Match consists of multiple Games, with a Game having two Winners and two Losers
 */
case class Game(id: Pk[Long], matchId: Long, winner1: String, winner2: String, loser1: String, loser2: String, score: Game.Score.Value) {
  require(!winner1.isEmpty() && !winner2.isEmpty() && !loser1.isEmpty() && !loser2.isEmpty())  
  require(winner1 != winner2 && winner1 != loser1 && winner1 != loser2)
  require(winner2 != loser1 && winner2 != loser2)
  require(loser1 != loser2)
  
  val winners = List(winner1, winner2)
  val losers  = List(loser1, loser2)
  val players = winners ++ losers
  
  def playerScore(player: String): Int =
    if (winner1 == player || winner2 == player) 2 // Won 2-0 or 2-1
    else if (score == Game.Score.Two_One) 1       // Lost 2-1
    else 0                                        // Lost 2-0
    
  /**
   * Compares the results of this Game with another.
   * The Winners are compared with each other, the Losers with each other and the Scores.
   * The Id and Match Id are ignored.
   */
  def equalsResults(other: Game): Boolean =
    winners.toSet == other.winners.toSet &&
    losers.toSet == other.losers.toSet &&
    score == other.score
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
  
  def findByMatch(matchId: Long): Seq[Game] = DB.withConnection { implicit connection =>
    SQL("select * from game where match_id = {matchId}").on('matchId -> matchId).as(Game.simple *)
  }
  
  // TODO: Be careful, this will be vulnerable to SQL injection if the parameterised type is String instead of Long
  //       Probably need to do something complex like http://nineofclouds.blogspot.com/2013/04/in-clause-with-anorm.html
  def findByMatches(matchIds: Iterable[Long]): Map[Long, List[Game]] = 
    if (matchIds.isEmpty) Map()
    else { DB.withConnection { implicit connection =>
      val games = SQL("select * from game where match_id in (%s)" format matchIds.mkString(",")).as(Game.simple *)
      games.groupBy(_.matchId)
    }}
  
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