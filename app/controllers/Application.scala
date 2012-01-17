package controllers

import play._
import play.i18n._
import play.mvc._

import models._
import results.Redirect
import views.Application._
import javax.mail.Session
import java.awt.Desktop.Action

object Application extends Controller {

  def login = {
    if (!session.isEmpty) {
      Action(Dashboard.show)
    } else {
      val users = User.find().list()
      html.login(users)
    }
  }

  def logout = {
    session.clear
    Action(Application.login)
  }

  def authenticate = {
    val name = params.get("name")
    val password = params.get("password")

    val result = User.authenticate(name, password)
    result match {
      case Some(user)  => session.put("username", user.name);
                          Action(Dashboard.show)
      case None        => flash.put("error", Messages.get("error.password"))
                          Action(Application.login)
    }
  }

}
