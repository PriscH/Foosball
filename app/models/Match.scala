package models

import scala.language.postfixOps

import org.joda.time.DateTime

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import util.db.AnormExtension.rowToDateTime

case class Match(id: Long, capturedDate: DateTime, capturedBy: String, confirmedBy: Option[String], format: String)

object Match {
  
  // ===== ResultSet Parsers =====
  
  val simple = {
    get[Long]           ("match.id") ~
    get[DateTime]       ("match.capturedDate") ~
    get[String]         ("match.capturedBy") ~
    get[Option[String]] ("match.confirmedBy") ~
    get[String]         ("match.format") map {
      case id ~ capturedDate ~ capturedBy ~ confirmedBy ~ format => Match(id, capturedDate, capturedBy, confirmedBy, format)
    }
  }
  
  // ===== Query Operations ======
  
  def all(): Seq[Match] = DB.withConnection { implicit connection =>
    SQL("select * from match").as(Match.simple *)
  }
  
  // ===== Persistance Operations =====

  def create(foosMatch: Match): Match = DB.withConnection { implicit connection =>
    SQL("insert into match values ({capturedDate}, {capturedBy}, {format})").on(
      'capturedDate -> foosMatch.capturedDate,
      'capturedBy   -> foosMatch.capturedBy,
      'format       -> foosMatch.format
    ).executeUpdate()
    
    return foosMatch
  }
}