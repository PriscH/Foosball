package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.libs.Codecs
import models._
import views._
import views.html.defaultpages.badRequest
import util.security._
import org.joda.time.DateTime


object Signup extends Controller with Secured {
  
  // ===== Forms =====
  
  val createPlayerForm = Form(
    tuple(
      "name"     -> text,
      "email"    -> text,
      "password" -> text,
      "avatar"   -> text,
      "token"    -> text
    )
  )
  
  // ===== Actions =====
  
  def show(token: String) = Action { implicit request => 
    val usedAvatars = User.all.map(_.avatar)

    Token.findByValue(token) match {
      case Some(dbToken) => Ok(html.signup.player(usedAvatars, dbToken))
      case None          => Ok(html.signup.expired())
    }
  }
  
  def createPlayer() = Action { implicit request => 
    val (name, email, password, avatar, token) = createPlayerForm.bindFromRequest.get
    
    if (name.length() < 2 || name.length > 10)      Redirect(routes.Signup.show(token)).flashing("error" -> Messages("name.length.error"))
    else if (password.length() < 6)                 Redirect(routes.Signup.show(token)).flashing("error" -> Messages("password.length.error"))
    else if (User.findByName(name).isDefined)       Redirect(routes.Signup.show(token)).flashing("error" -> Messages("name.unavailable.error"))
    else if (User.findWithAvatar(avatar).isDefined) Redirect(routes.Signup.show(token)).flashing("error" -> Messages("avatar.unavailable.error"))
    else {
      User.create(User(name, email, Codecs.sha1(password), avatar))
      Token.delete(token)
      Redirect(routes.Application.showLogin)
    }
  }
  
}