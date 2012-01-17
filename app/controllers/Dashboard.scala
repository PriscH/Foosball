package controllers

import play.mvc._

import views.Dashboard._
import util.security.Secure

object Dashboard extends Controller with Secure {

  def show = {
    val username = session.get("username");
    html.dashboard(username)
  }
  
}