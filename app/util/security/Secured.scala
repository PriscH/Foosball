package util.security

import controllers._
import play.api.mvc._

trait Secured {
  self:Controller =>
      
  // ===== Secured Actions =====
  
  def SecuredAction(action: => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) {
    user => Action(request => action(request))
  }
  
  // ===== Security Helpers =====
  
  private def username(request: RequestHeader): Option[String] = request.session.get("username")
  
  private def onUnauthorized(request: RequestHeader): Result = Results.Redirect(routes.Application.login)
}