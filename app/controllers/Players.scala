package controllers

import models.User
import play.api.libs.json.{JsString, JsNumber, JsValue, Json}
import play.api.mvc.Controller
import services.MatchService
import util.security.Secured
import views.html

object Players extends Controller with Secured {

  def show = SecuredAction { implicit user => implicit request => {
    val players = User.all
    val recentMatches = MatchService.findAllRecentMatches();

    val playerChances = players.map { player =>
      player.name -> {
        val mostRecentMatchIndex = recentMatches.indexWhere(_.players.contains(player.name))
        mostRecentMatchIndex match {
          case 0 => 2
          case 1 => 3
          case 2 => 4
          case -1 => 5
        }
      }
    }.toMap

    Ok(html.players.select(User.all, loadPlayerChancesJson(playerChances)))
  }}

  // ===== Chance Json Builders =====
  // TODO: JSon ... properly ...

  private def loadPlayerChancesJson(playerChances: Map[String, Int]): JsValue = Json.toJson(
    playerChances.map{ case (player, chances) => player -> JsNumber(chances) }
  )
}
