package controllers

import play.mvc._

import models._
import views.dashboard._
import util.security.Secure

object Dashboard extends Controller with Secure {

  def show = {
    val users = User.find().list()
    html.dashboard(users)
  }
  
}