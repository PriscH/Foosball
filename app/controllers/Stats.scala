package controllers

import controllers.Players._
import models.User
import play.api.libs.json.{JsNumber, Json, JsValue}
import play.api.mvc.Controller
import services.{StatisticsService, MatchService}
import util.security.Secured
import views.html

object Stats extends Controller with Secured {
  def show = SecuredAction { implicit user => implicit request => {
    val statistics = StatisticsService.loadStatistics()
    Ok(html.stats.show(statistics))
  }}
}
