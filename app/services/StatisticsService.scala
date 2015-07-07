package services

import domain._
import models.{Game, MatchResult, PlayerElo}

import scala.collection.mutable

object StatisticsService {

  val NumberOfRecords = 3

  // ===== Interface =====

  def loadStatistics(): Statistics = {
    val playerElos = PlayerElo.all()
    val matchesWithResults = MatchService.findAllMatchesWithResults()
    val matchesWithGames = MatchService.findAllMatchesWithGames()

    Statistics(
      determineHighestElos(playerElos),
      determineLowestElos(playerElos),
      determineLongestWinningStreaks(matchesWithResults),
      determineLongestLosingStreaks(matchesWithResults),
      determineHighestScoreDifferences(matchesWithGames),
      determineStrongestPartnerships(matchesWithGames.flatMap(_.games)),
      determineWeakestPartnerships(matchesWithGames.flatMap(_.games)),
      determinePlayerNemesis(matchesWithGames)
    )
  }

  // ===== Statistics =====

  private def determineHighestElos(playerElos: Seq[PlayerElo]): Seq[SinglePlayerRecord] = determineBoundaryElos(playerElos, Ordering[Double].reverse)

  private def determineLowestElos(playerElos: Seq[PlayerElo]): Seq[SinglePlayerRecord] = determineBoundaryElos(playerElos, Ordering[Double])

  private def determineLongestWinningStreaks(matchesWithResults: Seq[MatchWithResults]): Seq[SinglePlayerRecord] = determineLongestStreaks(matchesWithResults, MatchResult.Result.Winner)

  private def determineLongestLosingStreaks(matchesWithResults: Seq[MatchWithResults]): Seq[SinglePlayerRecord] = determineLongestStreaks(matchesWithResults, MatchResult.Result.Loser)

  private[services] def determineHighestScoreDifferences(matchesWithGames: Seq[MatchWithGames]): Seq[MatchRecord] = {
    val gamesWithDates = for (
      foosMatch <- matchesWithGames;
      game <- foosMatch.games
    ) yield (foosMatch.capturedDate, game)

    val highestScoreDifferences = gamesWithDates.filter(_._2.goalDifference > 0).sortWith{
      (x, y) => if (x._2.goalDifference == y._2.goalDifference) x._1.isAfter(y._1) else x._2.goalDifference > y._2.goalDifference
    }.take(NumberOfRecords)

    highestScoreDifferences.map(gameWithDate => MatchRecord(gameWithDate._2.winners, gameWithDate._2.losers, Record(gameWithDate._2.goalDifference, Some(gameWithDate._1))))
  }

  private def determineStrongestPartnerships(games: Seq[Game]): Seq[PartnershipRecord] = determineBoundaryPartnerships(games, Ordering[Double].reverse)

  private def determineWeakestPartnerships(games: Seq[Game]): Seq[PartnershipRecord] = determineBoundaryPartnerships(games, Ordering[Double])

  private[services] def determinePlayerNemesis(matchesWithGames: Seq[MatchWithGames]): Map[String, String] = {
    val matchesByPlayer = matchesWithGames.foldLeft(new mutable.HashMap[String, mutable.Set[MatchWithGames]] with mutable.MultiMap[String, MatchWithGames]) {
      (acc, foosMatch) => {
        foosMatch.players.foreach(player => acc.addBinding(player, foosMatch))
        acc
      }
    }

    val nemesisScoresByPlayer = matchesByPlayer.map { case (player, matches) => player ->
      matches.filter(_.players.contains(player)).foldLeft(new mutable.HashMap[String, (Int, Long)].withDefaultValue((0, 0L))) {
        (acc, foosMatch) => {
          foosMatch.players.filter(_ != player).foreach { nemesis =>
            val accScore = acc(nemesis)
            acc(nemesis) = (accScore._1 + 1, accScore._2 + foosMatch.goalDifference(player))
          }
          acc
        }
      }
    }

    val averageNemesisScores = nemesisScoresByPlayer.mapValues { scoresByNemesis => scoresByNemesis.map{ case (nemesis, score) => (nemesis, score._2 / score._1.toDouble)}.toList }
    averageNemesisScores.mapValues(nemesisScores => nemesisScores.sortBy(_._2).head._1).toMap
  }

  // ===== Calculation Helpers ======

  private[services] def determineBoundaryElos(playerElos: Seq[PlayerElo], ordering: Ordering[Double]): Seq[SinglePlayerRecord] = {
    val boundaryElosByPlayer = playerElos.groupBy(_.player).mapValues(_.sortBy(_.elo)(ordering).head)
    val boundaryElos = boundaryElosByPlayer.values.toList.sortWith{
      (x, y) => if (x.elo == y.elo) x.capturedDate.isAfter(y.capturedDate) else ordering.lt(x.elo, y.elo)
    }.take(NumberOfRecords)

    boundaryElos.map(playerElo => SinglePlayerRecord(playerElo.player, Record(Math.round(playerElo.elo).toInt, Some(playerElo.capturedDate))))
  }

  private[services] def determineLongestStreaks(matchesWithResults: Seq[MatchWithResults], result: MatchResult.Result): Seq[SinglePlayerRecord] = {
    def groupByStreaks(matches: Seq[MatchWithResults], player: String): Seq[Seq[MatchWithResults]] = matches match {
      case Seq()        => Seq()
      case head +: tail => {
        if (head.playerWithResult(result) == Some(player)) {
          val segment = matches.takeWhile(_.playerWithResult(result) == Some(player))
          segment +: groupByStreaks(matches.drop(segment.length), player)
        } else {
          val segment = matches.takeWhile(_.playerWithResult(result) != Some(player))
          groupByStreaks(matches.drop(segment.length), player)
        }
      }
    }

    val matchesByPlayer = matchesWithResults.foldLeft(new mutable.HashMap[String, mutable.Set[MatchWithResults]] with mutable.MultiMap[String, MatchWithResults]) {
      (acc, foosMatch) => {
        foosMatch.players.foreach(player => acc.addBinding(player, foosMatch))
        acc
      }
    }

    val streaks = matchesByPlayer.flatMap { case (player, matches) =>
      val sortedMatches = matches.toList.sortWith{(a, b) => a.foosMatch.capturedDate.isBefore(b.foosMatch.capturedDate)}
      groupByStreaks(sortedMatches, player)
    }

    val longestStreaks = streaks.toList.sortWith{
      (x, y) => if (x.length == y.length) x.last.foosMatch.capturedDate.isAfter(y.last.foosMatch.capturedDate) else x.length > y.length
    }.take(NumberOfRecords)

    longestStreaks.map(streak => SinglePlayerRecord(streak.head.playerWithResult(result).get, Record(streak.length, Some(streak.last.foosMatch.capturedDate))))
  }

  private[services] def determineBoundaryPartnerships(games: Seq[Game], ordering: Ordering[Double]): Seq[PartnershipRecord] = {
    val goalDifferencesByPartnership = games.foldLeft(new mutable.HashMap[Set[String], (Int, Long)].withDefaultValue((0, 0L))) {
      (acc, game) => {
        val accScoreWinners = acc(game.winners.toSet)
        acc(game.winners.toSet) = (accScoreWinners._1 + 1, accScoreWinners._2 + game.goalDifference)

        val accScoreLosers = acc(game.losers.toSet)
        acc(game.losers.toSet) = (accScoreLosers._1 + 1, accScoreLosers._2 - game.goalDifference)

        acc
      }
    }

    val averagePartnershipScores = goalDifferencesByPartnership.mapValues(scores => scores._2 / scores._1.toDouble).toList
    val strongestPartnerships = averagePartnershipScores.sortBy(x => x._2)(ordering).take(NumberOfRecords)

    strongestPartnerships.map(partnershipScore => PartnershipRecord(partnershipScore._1.toList, Record(Math.round(partnershipScore._2).toInt, None)))
  }
}
