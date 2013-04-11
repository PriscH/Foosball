package models

import scala.language.postfixOps

import org.joda.time.DateTime

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import util.db.AnormExtension.rowToDateTime

/**
 * A security token which can be used to allow access to portions of the site, such as the signup page.
 */
case class Token(value: String, scope: Token.Scope.Value, capturedDate: DateTime)

object Token {
  
  object Scope extends Enumeration {
    val Signup = Value("Signup")
  }
  
  val InitialToken = "initial"

  // ===== ResultSet Parsers =====
  
  val simple = {
    get[String]   ("token.value") ~
    get[String]   ("token.scope") ~
    get[DateTime] ("token.captured_date") map {
      case value ~ scope ~ capturedDate => Token(value, Scope.withName(scope), capturedDate)
    }
  }
  
  // ===== Query Operations ======
  
  def all(): Seq[Token] = DB.withConnection { implicit connection =>
    SQL("select * from token").as(Token.simple *)
  }
  
  def findByValue(value: String): Option[Token] = DB.withConnection { implicit connection =>
    SQL("select * from token where value = {value}").on('value -> value).as(Token.simple.singleOpt)
  }
  
  // ===== Persistance Operations =====

  def create(token: Token): Token = DB.withConnection { implicit connection =>
    SQL("insert into token (value, scope, captured_date) values ({value}, {scope}, {capturedDate})").on(
      'value        -> token.value,
      'scope        -> token.scope.toString,
      'capturedDate -> token.capturedDate.toDate
    ).executeInsert()
    
    return token
  }
  
  def delete(token: String) = DB.withConnection { implicit connection =>
    SQL("delete from token where value = {value}").on('value -> token).executeUpdate()
  }
  
}