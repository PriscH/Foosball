package models

import scala.language.postfixOps
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import scala.collection.mutable.MultiMap

/**
 * A Match consists of multiple Games
 */
case class Game(id: Option[Long], matchId: Long, leftPlayer1: String, leftPlayer2: String, rightPlayer1: String, rightPlayer2: String, leftScore1: Int, leftScore2: Int, rightScore1: Int, rightScore2: Int) {
  require(!leftPlayer1.isEmpty() && !leftPlayer2.isEmpty() && !rightPlayer1.isEmpty() && !rightPlayer2.isEmpty())
  require(Set(leftPlayer1, leftPlayer2, rightPlayer1, rightPlayer2).size == 4)

  val leftPlayers = List(leftPlayer1, leftPlayer2)
  val rightPlayers = List(rightPlayer1, rightPlayer2)
  val players = leftPlayers ++ rightPlayers

  val leftTotal = leftScore1 + leftScore2
  val rightTotal = rightScore1 + rightScore2

  val gameDiff1 = Math.abs(leftScore1 - rightScore1)
  val gameDiff2 = Math.abs(leftScore2 - rightScore2)

  val leftResult = if (leftTotal == Game.MaxScore) Game.Result.WinInTwo
                   else if (rightTotal == Game.MaxScore) Game.Result.LoseInTwo
                   else if (leftTotal > rightTotal) Game.Result.WinOnScore
                   else if (leftTotal < rightTotal) Game.Result.LoseOnScore
                   else Game.Result.Draw

  val rightResult = if (rightTotal == Game.MaxScore) Game.Result.WinInTwo
                    else if (leftTotal == Game.MaxScore) Game.Result.LoseInTwo
                    else if (rightTotal > leftTotal) Game.Result.WinOnScore
                    else if (rightTotal < leftTotal) Game.Result.LoseOnScore
                    else Game.Result.Draw

  val winners = if (leftResult == Game.Result.WinInTwo || leftResult == Game.Result.WinOnScore) leftPlayers
                else if (rightResult == Game.Result.WinInTwo || rightResult == Game.Result.WinOnScore) rightPlayers
                else Nil

  val losers = if (leftResult == Game.Result.LoseInTwo || leftResult == Game.Result.LoseOnScore) leftPlayers
               else if (rightResult == Game.Result.LoseInTwo || rightResult == Game.Result.LoseOnScore) rightPlayers
               else Nil

  def goalDifference(player: String) = if (leftPlayers.contains(player)) leftTotal - rightTotal
                                       else rightTotal - leftTotal

  def goalDifference = Math.abs(leftTotal - rightTotal)

  /**
   * Compares the results of this Game with another.
   * The Winners are compared with each other, the Losers with each other and the Scores.
   * The Id and Match Id are ignored.
   */
  // TODO: This isn't complete, it can't differentiate if the players swap but the scores remain i.e. A 0 -  5 B compared with A 5 - 0 B
  def isSame(other: Game): Boolean =
    winners.toSet == other.winners.toSet &&
    losers.toSet == other.losers.toSet &&
    gameDiff1 == other.gameDiff1 &&
    gameDiff2 == other.gameDiff2
}

object Game {

  val MaxScore = 10

  sealed abstract class Result(val name: String, val value: Double) {
    override def toString = name
  }

  object Result {
    val Max = WinInTwo.value

    case object WinInTwo    extends Result("WinInTwo", 4.0)
    case object WinOnScore  extends Result("WinOnScore", 3.0)
    case object Draw        extends Result("Draw", 2.0)
    case object LoseOnScore extends Result("LoseOnScore", 1.0)
    case object LoseInTwo   extends Result("LoseInTwo", 0.0)

    val values = List(WinInTwo, WinOnScore, Draw, LoseOnScore, LoseInTwo)
    def apply(name: String) = values.find(_.name == name).get
  }

  // ===== Overloaded Apply =====
  
  // Allowing this is really ugly, but other than duplicating a portion of Game, I'm not sure how to avoid it
  // It is needed by the controllers to provide the services with the results of a Game, but at that stage the controllers don't have a match id
  def apply(leftPlayer1: String, leftPlayer2: String, rightPlayer1: String, rightPlayer2: String, leftScore1: Int, leftScore2: Int, rightScore1: Int, rightScore2: Int): Game = {
    Game(None, 0, leftPlayer1, leftPlayer2, rightPlayer1, rightPlayer2, leftScore1, leftScore2, rightScore1, rightScore2)
  }
      
  // ===== ResultSet Parsers =====
  
  val simple = {
    get[Option[Long]] ("game.id") ~
    get[Long]         ("game.match_id") ~
    get[String]       ("game.left_player1") ~
    get[String]       ("game.left_player2") ~
    get[String]       ("game.right_player1") ~
    get[String]       ("game.right_player2") ~
    get[Int]          ("game.left_score1") ~
    get[Int]          ("game.left_score2") ~
    get[Int]          ("game.right_score1") ~
    get[Int]          ("game.right_score2") map {
      case id ~ matchId ~ leftPlayer1 ~ leftPlayer2 ~ rightPlayer1 ~ rightPlayer2 ~ leftScore1 ~ leftScore2 ~ rightScore1 ~ rightScore2
            => Game(id, matchId, leftPlayer1, leftPlayer2, rightPlayer1, rightPlayer2, leftScore1, leftScore2, rightScore1, rightScore2)
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
    SQL("""
          insert into game (match_id, left_player1, left_player2, right_player1, right_player2, left_score1, left_score2, right_score1, right_score2)
                      values ({matchId}, {leftPlayer1}, {leftPlayer2}, {rightPlayer1}, {rightPlayer2}, {leftScore1}, {leftScore2}, {rightScore1}, {rightScore2})
        """).on(
      'matchId      -> game.matchId,
      'leftPlayer1  -> game.leftPlayer1,
      'leftPlayer2  -> game.leftPlayer2,
      'rightPlayer1 -> game.rightPlayer1,
      'rightPlayer2 -> game.rightPlayer2,
      'leftScore1   -> game.leftScore1,
      'leftScore2   -> game.leftScore2,
      'rightScore1  -> game.rightScore1,
      'rightScore2  -> game.rightScore2
    ).executeInsert().map(newId => game.copy(id = Some(newId))).get
  }
}