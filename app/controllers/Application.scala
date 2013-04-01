package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import models._
import views._

object Application extends Controller {
  
  // ===== Forms =====
  
  val loginForm = Form(
    tuple(
      "name" -> text,
      "password" -> text
    )
  )   

  // ===== Authentication Actions =====
  
  def login = Action { implicit request => {
    val users = User.all
    Ok(html.application.login(users))
  }}
  
  
  def authenticate = Action { implicit request =>
    val (name, password) = loginForm.bindFromRequest.get
    val authentication = User.authenticate(name, password)
        
    authentication match {
      case Some(user)  => Redirect(routes.Dashboard.show).withSession("username" -> user.name) 
      case None        => Redirect(routes.Application.login).flashing("error" -> Messages("password.error"))
    }
  }
  
}
