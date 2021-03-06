package controllers

import play.api.mvc.Controller
import play.api.libs.json.Json
import play.api.libs.json.JsValue

import models._
import services._
import views._
import util.security.Secured
import views.html.graphs.history
import akka.actor.FSM.->

object Graphs extends Controller with Secured {

  // ===== Actions =====
  
  def showHistory = SecuredAction { implicit user => implicit request =>
    Ok(html.graphs.history(User.all, loadDefaultSelectedPlayers, loadGraphDataJson, Match.countAllMatches()))
  }

  // ===== Data Loaders  =====

  private def loadDefaultSelectedPlayers(implicit user: User): Seq[String] = {
    val latestPlayerElos = PlayerElo.findLatestElos();
    
    if (latestPlayerElos.isEmpty) {
      PlayerService.findMostRecentOpponents
    } else {
      val highestPlayer = latestPlayerElos.maxBy(_.elo).player
      val lowestPlayer = latestPlayerElos.minBy(_.elo).player

      val defaultPlayers = PlayerService.findMostRecentOpponents :+ highestPlayer :+ lowestPlayer
      defaultPlayers.distinct
    }
  }

  // ===== Graph Json Builders =====
  // TODO: JSon ... properly ...

  private def loadGraphDataJson(): JsValue = Json.toJson(
  GraphService.loadHistoryGraphData.map { case (player, history) =>
      Json.toJson(Map(
        "label" -> Json.toJson(player),
        "data"  -> Json.toJson(history.map { case (index, elo) =>
          Seq(index, elo)
        }),
        "lines" -> Json.toJson(Map(
          "show" -> true
        ))
      ))
    }
  )
}