package models

import scala.language.postfixOps

import play.api.db._
import play.api.Play.current
import play.api.libs.Codecs

import anorm._
import anorm.SqlParser._

case class User(name: String, email: String, password: String, avatar: String)

object User {
  
  // ===== ResultSet Parsers =====
  
  val simple = {
  	get[String] ("user.name") ~
  	get[String] ("user.email") ~
  	get[String] ("user.password") ~
  	get[String] ("user.avatar") map {
  	  case name ~ email ~ password ~ avatar => User(name, email, password, avatar)
  	}
  }
  
  // ===== Query Operations ======
  
  def all(): Seq[User] = DB.withConnection { implicit connection =>
    SQL("select * from user").as(User.simple *)
  }
  
  // ===== Persistance Operations =====

  def create(user: User): User = DB.withConnection { implicit connection =>
    SQL("insert into user values ({name}, {email}, {password}, {avatar})").on(
      'name     -> user.name,
      'email    -> user.email,
      'password -> user.password,
      'avatar   -> user.avatar
    ).executeUpdate()
    
    return user
  }
  
  // ===== Functional Operations =====
  
  def authenticate(name: String, password: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from user where name = {name} and password = {password}").on(
        'name -> name,
        'password -> Codecs.sha1(password)
      ).as(User.simple.singleOpt)
    }
  }
}