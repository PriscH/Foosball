package services

import models._

object GraphService {

  // ===== Interface =====
  
  def loadHistoryGraph(): Map[String, Seq[(Int, Double)]] = {
    val playerElos = EloService.loadCompleteElos
    val datesWithIndex = playerElos.map(_.capturedDate).distinct.sortBy(_.getMillis).zipWithIndex.toMap
    
    playerElos.groupBy(_.player).map {case (player, elos) => player -> 
      (elos.sortBy(_.capturedDate.getMillis()).map {elo =>
        datesWithIndex(elo.capturedDate) -> elo.elo 
      })
    }.toMap
  }
  
}