package models

import scala.language.postfixOps
import org.joda.time.DateTime
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import util.db.AnormExtension._
import java.math.BigDecimal

/**
 * Tracks the history of changes to a player's elo, with the latest capturedDate representing the current elo
 */
case class PlayerElo(id: Pk[Long], player: String, capturedDate: DateTime, matchId: Long, change: Double, elo: Double)

object PlayerElo {

  // ===== ResultSet Parsers =====
  
  val simple = {
    get[Pk[Long]] ("player_elo.id") ~
    get[String]   ("player_elo.player") ~
    get[DateTime] ("player_elo.captured_date") ~
    get[Long]     ("player_elo.match_id") ~
    get[Double]   ("player_elo.elo_change") ~
    get[Double]   ("player_elo.elo") map {
      case id ~ player ~ capturedDate ~ matchId ~ change ~ elo => PlayerElo(id, player, capturedDate, matchId, change, elo)
    }
  }
  
  // ===== Query Operations ======
  
  def all(): Seq[PlayerElo] = DB.withConnection { implicit connection =>
    SQL("select * from player_elo").as(PlayerElo.simple *)
  }
  
  def findLatestElos(): Seq[PlayerElo] = DB.withConnection { implicit connection =>
    SQL("""
          select player_elo.* from player_elo
            inner join (
              select player, max(captured_date) as max_date from player_elo group by player) as latest_value
            on player_elo.player = latest_value.player
            and player_elo.captured_date = latest_value.max_date
        """).as(PlayerElo.simple *)
  }
  
  // ===== Persistance Operations =====

  def create(playerElo: PlayerElo): PlayerElo = DB.withConnection { implicit connection =>
    SQL("insert into player_elo (player, captured_date, match_id, elo_change, elo) values ({player}, {capturedDate}, {matchId}, {change}, {elo})").on(
      'player       -> playerElo.player,
      'capturedDate -> playerElo.capturedDate,
      'matchId      -> playerElo.matchId,
      'change       -> playerElo.change,
      'elo          -> playerElo.elo
    ).executeInsert().map(newId => playerElo.copy(id = Id(newId))).get
  } 
}