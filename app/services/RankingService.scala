package services

import domain._
import models._

object RankingService {

  // ===== Interface =====

  def loadCurrentRankings(): Seq[PlayerRanking] = {
    val players = User.all()
    val matchResults = MatchResult.findConfirmed()
    val playerElos = EloService.findCurrentElosWithChanges(players.map(_.name))

    players.map(player => {
      val playerElo = playerElos(player.name)
      val playerResults = matchResults.filter(_.player == player.name)

      PlayerRanking(player.name,
                    playerElos.count(_._2.elo > playerElo.elo) + 1,
                    playerResults.size,
                    playerResults.count(_.result == MatchResult.Result.Winner),
                    playerResults.count(_.result == MatchResult.Result.PseudoWinner),
                    playerResults.count(_.result == MatchResult.Result.PseudoLoser),
                    playerResults.count(_.result == MatchResult.Result.Loser),
                    playerElo.elo,
                    playerElo.change)
    })
  }
}