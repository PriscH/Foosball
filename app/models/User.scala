package models

import play.api.db._
import play.api.Play.current
import play.api.libs.Codecs

import anorm._
import anorm.SqlParser._

case class User(name: String, password: String, avatar: String)

object User {
  
  // ===== ResultSet Parsers =====
  
  val simple = {
  	get[String] ("user.name") ~
  	get[String] ("user.password") ~
  	get[String] ("user.avatar") map {
  	  case name ~ password ~ avatar => User(name, password, avatar)
  	}
  }
  
  // ===== Query Operations ======
  
  def all(): List[User] = DB.withConnection { implicit connection =>
    SQL("select * from user").as(User.simple *)
  }
  
  // ===== Persistance Operations =====

  def create(user: User): User = DB.withConnection { implicit connection =>
    SQL("insert into user values ({name}, {password}, {avatar})").on(
      'name     -> user.name,
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