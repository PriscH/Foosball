package util.security

import play.mvc._
import controllers.Application

trait Secure {
  self:Controller =>

  @Before
  def checkSecurity = {
    session("username") match {
      case Some(username) => Continue
      case None => Action(Application.login)
    }
  }
}