package controllers

import play.api._
import play.api.mvc._
import services._

object Migrations extends Controller {

  // TODO: This is dangerous and needs to be automated and secured
  def recalculateElo = Action { implicit request =>
    EloService.recalculateElo
    Ok("Recalculated the ELO values successfully.")
  } 
}