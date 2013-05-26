package controllers

import play.api.mvc.Controller
import play.api.libs.json.Json
import play.api.libs.json.JsValue

import models._
import services._
import views._
import util.security.Secured

object Graphs extends Controller with Secured {
  
  private val EloBuffer = 5; 

  // ===== Actions =====
  
  def showHistory = SecuredAction { implicit user => implicit request =>
    val historyGraph = GraphService.loadHistoryGraph    
    Ok(html.graphs.history(user.name, User.all, toJson(historyGraph), toConfigJson(historyGraph)))
  }
  
  // ===== Helpers =====
  
  // TODO: JSon ... properly ...
  private def toJson(historyGraph: Map[String, Seq[(Int, Double)]]): JsValue = Json.toJson(
    historyGraph.map { case (player, history) =>
      player -> history.map { case (index, elo) =>
        Seq(index, elo)
      }
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
    ))
  ))
}