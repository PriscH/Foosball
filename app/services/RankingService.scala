package services

import domain._
import models._

object RankingService {

  def loadCurrentRankings(): Seq[PlayerRanking] = {    
    val players = User.all()
    val playerElos = PlayerElo.findLatestElos()
    
    Nil
  }
  
}