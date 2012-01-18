package models

import play.db.anorm._
import play.db.anorm.defaults._
import play.libs.Codec

case class User(
  id: Pk[Long],
  name: String, password: String
)

object User extends Magic[User] {

  def authenticate(username: String, password: String) = {
    val hashedPassword = Codec.hexSHA1(password)

    find("name = {name} and password = {password}")
      .on("name" -> username, "password" -> hashedPassword)
      .first()
  }

}