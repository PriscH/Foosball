package models

import scala.language.postfixOps

import org.joda.time.DateTime

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import util.db.AnormExtension.rowToDateTime

case class Match(id: Pk[Long], capturedDate: DateTime, capturedBy: String, confirmedBy: Option[String], format: String)

object Match {
  
  // ===== Overloaded Apply =====
  
  def apply(id: Long, prototype: Match): Match = {
    Match(Id(id), prototype.capturedDate, prototype.capturedBy, prototype.confirmedBy, prototype.format)
  }
  
  // ===== ResultSet Parsers =====
  
  val simple = {
    get[Pk[Long]]       ("match.id") ~
    get[DateTime]       ("match.captured_date") ~
    get[String]         ("match.captured_by") ~
    get[Option[String]] ("match.confirmed_by") ~
    get[String]         ("match.format") map {
      case id ~ capturedDate ~ capturedBy ~ confirmedBy ~ format => Match(id, capturedDate, capturedBy, confirmedBy, format)
    }
  }
  
  // ===== Query Operations ======
  
  def all(): Seq[Match] = DB.withConnection { implicit connection =>
    SQL("select * from match").as(Match.simple *)
  }
  
  def findById(matchId: Long): Match = DB.withConnection { implicit connection =>
    SQL("select * from match where match.id = {matchId}").on('matchId -> {matchId}).single(Match.simple)
  }
  
  def findUnconfirmedFor(player: String): Seq[Match] = DB.withConnection { implicit connection =>
    SQL("""
        select match.* from match 
          left join match_result on (match.id = match_result.match_id)
          where match.confirmed_by is null
            and match.captured_by <> {player}
            and match_result.player = {player}
        """).on(
        'player -> player
      ).as(Match.simple *)
  }
  
  // ===== Persistance Operations =====

  def create(foosMatch: Match): Match = DB.withConnection { implicit connection =>
    SQL("insert into match (captured_date, captured_by, format) values ({capturedDate}, {capturedBy}, {format})").on(
      'capturedDate -> foosMatch.capturedDate.toDate(),
      'capturedBy   -> foosMatch.capturedBy,
      'format       -> foosMatch.format
    ).executeInsert() 
  } match {
    case Some(key) => Match(key, foosMatch)
    case None      => throw new RuntimeException("Could not create a match.")
  }
  
  def update(foosMatch: Match) = DB.withConnection { implicit connection => 
    SQL("""
        update match 
          set captured_date = {capturedDate},
              captured_by = {capturedBy}, 
              confirmed_by = {confirmedBy}, 
              format = {format}
          where id = {matchId}
        """).on(
        'capturedDate -> foosMatch.capturedDate.toDate(),
        'capturedBy   -> foosMatch.capturedBy,
        'confirmedBy  -> foosMatch.confirmedBy,
        'format       -> foosMatch.format,
        'matchId      -> foosMatch.id
      ).executeUpdate() 
  }
}