package services

import org.joda.time.DateTime

import models._
import domain._

object EloService {

  private val StartingElo = 1000.0
  private val StartingChange = 0.0
  private val EloWeight = 400.0

  private val ResultWeight = 0.5
  private val GoalWeight = 1.0 - ResultWeight

  // ===== Interface =====

  def findCurrentElos(players: Seq[String]): Map[String, Double] = {
    findCurrentElos(players, (elo: PlayerElo) => elo.elo, StartingElo)
  }

  def findCurrentElosWithChanges(players: Seq[String]): Map[String, EloWithChange] = {
    findCurrentElos(players, (elo: PlayerElo) => (elo.elo, elo.change), (StartingElo, StartingChange))
  }

  // Elo calculation based on http://sradack.blogspot.com/2008/06/elo-rating-system-multiple-players.html
  // And http://math.stackexchange.com/questions/1084436/two-leg-games-in-elo-rating-for-football-teams
  def updateElo(matchWithGames: MatchWithGames) = {
    val players = matchWithGames.players
    val currentElos = findCurrentElos(players)
    val eloChanges = calculateMatchEloChanges(matchWithGames, currentElos)

    matchWithGames.players.map(player => {
      val previousElo = currentElos(player)
      val eloChange = eloChanges(player)
      val updatedElo = previousElo + eloChange

      PlayerElo.create(PlayerElo(None, player, matchWithGames.capturedDate, matchWithGames.matchId, eloChange, updatedElo))
    })
  }
  
  // Loads all the explicit elos and generates an implicit starting elo for each player
  def loadCompleteElos(): Seq[PlayerElo] = {
    val players = User.all.map(_.name)
    val explicitElos = PlayerElo.all
    val eloDates = explicitElos.map(_.capturedDate)

    // Assign the starting elo date to the last match captured prior to the players first explicit elo
    // so as to avoid introducing "fake" matches
    val implicitElos = for {
      player <- players
      firstDate = explicitElos.filter(_.player == player).sortBy(_.capturedDate.getMillis).headOption match {
        case Some(elo) => if (eloDates.exists(_.isBefore(elo.capturedDate))) eloDates.filter(_.isBefore(elo.capturedDate)).sortBy(_.getMillis).last
                          else elo.capturedDate.minusDays(1)
        case None      => if (eloDates.isEmpty) new DateTime()
                          else eloDates.sortBy(_.getMillis).last
      }
    } yield PlayerElo(None, player, firstDate, 0, StartingChange, StartingElo)
    
    implicitElos ++ explicitElos
  }
  
  /**
   * Will recalculate the Elo values for all the Players for all Matches.
   * Should only be used as part of a migration script in response to changes to the Elo calculation algorithm
   */
  def recalculateElo() {
    val matchesWithGames = MatchWithGames.all.sortWith{(a, b) => a.capturedDate.isBefore(b.capturedDate)}

    // Remove the old Elos
    PlayerElo.clear

    // Recalculate in the same order as the initial calculation
    matchesWithGames.map(updateElo(_))
  }

  // ===== Helper Methods =====

  private def findCurrentElos[V](players: Seq[String], valueMapping: (PlayerElo => V), default: V): Map[String, V] = {
    val currentElos = PlayerElo.findLatestElos()
    players.map(player => player -> {
      currentElos.find(_.player == player) match {
        case Some(elo) => valueMapping(elo)
        case None      => default
      }
    }).toMap
  }

  private def calculateMatchEloChanges(matchWithGames: MatchWithGames, startElos: Map[String, Double]): Map[String, Double] = {
    val zeroMap = matchWithGames.players.map(_ -> 0.0).toMap
    matchWithGames.games.foldLeft(zeroMap) {(previousEloChanges, game) => {
      val gameEloChanges = calculateGameEloChanges(game, matchWithGames.format, startElos)
      gameEloChanges.map{case (player, eloChange) => player -> (eloChange + previousEloChanges(player))}
    }}
  }

  private def calculateGameEloChanges(game: Game, format: Match.Format, startElos: Map[String, Double]): Map[String, Double] = {
    val leftEloAverage = (startElos(game.leftPlayer1) + startElos(game.leftPlayer2)) / 2
    val rightEloAverage = (startElos(game.rightPlayer1) + startElos(game.rightPlayer2)) / 2

    val leftExpectedScore = estimateScoreVersus(leftEloAverage, rightEloAverage)
    val rightExpectedScore = 1.0 - leftExpectedScore

    val resultBasedEloChanges = calculateResultBasedEloChanges(game, format, leftExpectedScore, rightExpectedScore)
    val goalBasedEloChanges = calculateGoalBasedEloChanges(game, format, leftExpectedScore, rightExpectedScore)

    game.players.map { player =>
      player -> (resultBasedEloChanges(player) * ResultWeight + goalBasedEloChanges(player) * GoalWeight)
    }.toMap
  }

  private def calculateResultBasedEloChanges(game: Game, format: Match.Format, leftExpectedScore: Double, rightExpectedScore: Double): Map[String, Double] = {
    val leftActualScore = game.leftResult.value / Game.Result.Max
    val rightActualScore = game.rightResult.value / Game.Result.Max

    game.players.map{ player =>
      val eloChange = if (game.leftPlayers.contains(player)) format.kValue * (leftActualScore - leftExpectedScore)
      else format.kValue * (rightActualScore - rightExpectedScore)
      (player, eloChange)
    }.toMap
  }

  private def calculateGoalBasedEloChanges(game: Game, format: Match.Format, leftExpectedScore: Double, rightExpectedScore: Double) = {
    def normalizeScore(goalDifference: Int): Double = {
      (goalDifference + Game.MaxScore) / (2.0 * Game.MaxScore)
    }

    val leftGoalDifference = (game.leftTotal - game.rightTotal)
    val rightGoalDifference = (game.rightTotal - game.leftTotal)

    val leftActualScore = normalizeScore(leftGoalDifference)
    val rightActualScore = normalizeScore(rightGoalDifference)

    game.players.map{ player =>
      val eloChange = if (game.leftPlayers.contains(player)) format.kValue * (leftActualScore - leftExpectedScore)
      else format.kValue * (rightActualScore - rightExpectedScore)
      (player, eloChange)
    }.toMap
  }

  // Visible for testing
  private[services] def estimateScoreVersus(playerElo: Double, opponentElo: Double): Double = 1.0 / (1.0 + math.pow(10, ((opponentElo - playerElo) / EloWeight)))

}