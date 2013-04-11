package util.security

import controllers._
import models._
import play.api.mvc._

trait Secured {
  self:Controller =>
      
  // ===== Secured Actions =====
  
  def SecuredAction(action: => User => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) { username =>
    Action(request => {
      User.findByName(username).map { 
        user => action(user)(request)
      }.getOrElse(onUnauthorized(request))
    })
  }
  
  // ===== Security Helpers =====
  
  private def username(request: RequestHeader): Option[String] = request.session.get("username")
  
  private def onUnauthorized(request: RequestHeader): Result = Results.Redirect(routes.Application.login)
}