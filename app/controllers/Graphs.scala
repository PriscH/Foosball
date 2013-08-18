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
  
  private val EloBuffer = 5; 

  // ===== Actions =====
  
  def showHistory = SecuredAction { implicit user => implicit request =>
    val historyGraph = GraphService.loadHistoryGraph
    val recentPlayers = PlayerService.findMostRecentOpponents
    Ok(html.graphs.history(User.all, recentPlayers, toJson(historyGraph), toConfigJson(historyGraph), toPlayerConfigJson(historyGraph)))
  }
  
  // ===== Helpers =====
  
  // TODO: JSon ... properly ...
  private def toJson(historyGraph: Map[String, Seq[(Int, Double)]]): JsValue = Json.toJson(
    historyGraph.map { case (player, history) =>
      player -> Json.toJson(Map(
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
  
  // TODO: JSon ... properly ...
  private def toConfigJson(historyGraph: Map[String, Seq[(Int, Double)]]): JsValue = Json.toJson(Map(
    "xaxis" -> Json.toJson(Map(
      "min"          -> 0,
      "max"          -> (historyGraph.values.flatten.maxBy(_._1)._1),
      "minTickSize"  -> 1,
      "tickDecimals" -> 0
    )),
    "yaxis" -> Json.toJson(Map(
      "min"          -> (historyGraph.values.flatten.minBy(_._2)._2.toInt - EloBuffer),
      "max"          -> (historyGraph.values.flatten.maxBy(_._2)._2.toInt + EloBuffer),
      "minTickSize"  -> 1,
      "tickDecimals" -> 0
    )),
    "legend" -> Json.toJson(Map(
      "show"         -> Json.toJson(true),
      "position"     -> Json.toJson("nw"),
      "margin"       -> Json.toJson(10)
    ))
  ))

  // TODO: JSon ... properly ...
  private def toPlayerConfigJson(historyGraph: Map[String, Seq[(Int, Double)]]): JsValue = Json.toJson(
    historyGraph.map { case (player, history) =>
      player -> Json.toJson(Map(
        "xaxis" -> Json.toJson(Map(
          "min"          -> (history.map(_._1).min),
          "max"          -> (history.map(_._1).max),
          "minTickSize"  -> 1,
          "tickDecimals" -> 0
        )),
        "yaxis" -> Json.toJson(Map(
          "min"          -> (history.map(_._2).min.toInt - EloBuffer),
          "max"          -> (history.map(_._2).max.toInt + EloBuffer),
          "minTickSize"  -> 1,
          "tickDecimals" -> 0
        )),
        "legend" -> Json.toJson(Map(
          "show"         -> Json.toJson(true),
          "position"     -> Json.toJson("nw"),
          "margin"       -> Json.toJson(5)
        ))
      ))
    }
  )
}