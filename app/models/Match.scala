package models

import scala.language.postfixOps

import org.joda.time.DateTime

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import util.db.AnormExtension._

/**
 * A Foosball Match, which is in a certain format and has someone that captures it
 */
case class Match(id: Pk[Long], capturedDate: DateTime, capturedBy: String, format: Match.Format.Value)

object Match {
  
  object Format extends Enumeration {
    val TwoOnTwo = Value("Two-on-Two")
  }
    
  // ===== ResultSet Parsers =====
  
  val simple = {
    get[Pk[Long]]       ("match_detail.id") ~
    get[DateTime]       ("match_detail.captured_date") ~
    get[String]         ("match_detail.captured_by") ~
    get[String]         ("match_detail.format") map {
      case id ~ capturedDate ~ capturedBy ~ format => Match(id, capturedDate, capturedBy, Format.withName(format))
    }
  }
  
  // ===== Query Operations ======
  
  def all(): Seq[Match] = DB.withConnection { implicit connection =>
    SQL("select * from match_detail").as(Match.simple *)
  }
  
  def findById(matchId: Long): Match = DB.withConnection { implicit connection =>
    SQL("select * from match_detail where id = {matchId}").on('matchId -> {matchId}).single(Match.simple)
  }
  
  // ===== Persistance Operations =====

  def create(foosMatch: Match): Match = DB.withConnection { implicit connection =>
    SQL("insert into match_detail (captured_date, captured_by, format) values ({capturedDate}, {capturedBy}, {format})").on(
      'capturedDate -> foosMatch.capturedDate.toDate(),
      'capturedBy   -> foosMatch.capturedBy,
      'format       -> foosMatch.format.toString
    ).executeInsert().map(newId => foosMatch.copy(id = Id(newId))).get
  }
}