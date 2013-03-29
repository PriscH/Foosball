package controllers

import play.api._
import play.api.mvc._
import models._
import views._
import util.security._

object Dashboard extends Controller with Secured {

  def show = SecuredAction { implicit request => {
    val users = User.all
    Ok(html.dashboard.index(users))
  }}
  
}