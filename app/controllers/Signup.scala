package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._
import views._
import util.security._

object Signup extends Controller with Secured {
  
  def show(token: String) = Action { implicit request => 
    Token.findByValue(token) match {
      case Some(dbToken) => {
        Token.delete(dbToken)
        Ok(html.signup.player())
      }
      case None          => Ok(html.signup.player())
    }
  }
  
}