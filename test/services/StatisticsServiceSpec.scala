package services

import domain.{MatchWithGames, MatchWithResults}
import models.{Game, MatchResult, Match, PlayerElo}
import org.joda.time.DateTime
import org.specs2.mutable._

class StatisticsServiceSpec extends Specification {

  // ===== Tests =====

  "The boundary Elos" should {
    "be empty when no matches have been played" in {
      val lowestElos = StatisticsService.determineBoundaryElos(Seq(), Ordering[Double])
      lowestElos must beEmpty
    }

    "only contain a single elo per player" in {
      val playerElos = List(
        buildPlayerElo(1, "Ronaldo", 1100.0),
        buildPlayerElo(2, "Ronaldo", 1000.0),
        buildPlayerElo(3, "Ronaldo", 1200.0)
      )

      val lowestElos = StatisticsService.determineBoundaryElos(playerElos, Ordering[Double])

      lowestElos must haveSize(1)
      lowestElos.head.record.value must beEqualTo(1000)
    }

    "contain the lowest elos" in {
      val playerElos = List(
        buildPlayerElo(1, "Ronaldo",  1000.0),
        buildPlayerElo(2, "Zidane",   800.0),
        buildPlayerElo(3, "Maradona", 1100.0),
        buildPlayerElo(4, "Messi",    1200.0),
        buildPlayerElo(5, "Zidane",   1100.0),
        buildPlayerElo(6, "Ronaldo",  900.0)
      )

      val lowestElos = StatisticsService.determineBoundaryElos(playerElos, Ordering[Double])

      lowestElos must haveSize(3)
      lowestElos.map(x => (x.player, x.record.value)) must contain(exactly(
        ("Zidane", 800), ("Ronaldo", 900), ("Maradona", 1100)
      ))
    }

    "contain the latest elos" in {
      val playerElos = List(
        buildPlayerElo(1, "Ronaldo",  1000.0),
        buildPlayerElo(2, "Zidane",   1000.0),
        buildPlayerElo(3, "Maradona", 1000.0),
        buildPlayerElo(4, "Messi",    1000.0)
      )

      val lowestElos = StatisticsService.determineBoundaryElos(playerElos, Ordering[Double])

      lowestElos must haveSize(3)
      lowestElos.map(x => (x.player, x.record.value)) must contain(exactly(
        ("Messi", 1000), ("Maradona", 1000), ("Zidane", 1000)
      ))
    }
  }

  "The longest streaks" should {
    "be empty when no matches have been played" in {
      val longestWinningStreaks = StatisticsService.determineLongestStreaks(Seq(), MatchResult.Result.Winner)
      longestWinningStreaks must beEmpty
    }

    "be empty when no match has been won" in {
      val matches = List(
        buildMatchForResult(1, "Ronaldo", "Zidane", "Maradona", "Messi", None, Some("Ronaldo")),
        buildMatchForResult(2, "Ronaldo", "Zidane", "Maradona", "Messi", None, Some("Ronaldo")),
        buildMatchForResult(3, "Ronaldo", "Zidane", "Maradona", "Messi", None, Some("Zidane"))
      )

      val longestWinningStreaks = StatisticsService.determineLongestStreaks(matches, MatchResult.Result.Winner)
      longestWinningStreaks must beEmpty
    }

    "be interrupted if the player played and did not win" in {
      val matches = List(
        buildMatchForResult(1, "Ronaldo", "Zidane", "Maradona", "Messi", Some("Ronaldo"), None),
        buildMatchForResult(2, "Ronaldo", "Zidane", "Maradona", "Messi", Some("Ronaldo"), None),
        buildMatchForResult(3, "Ronaldo", "Zidane", "Maradona", "Messi", Some("Ronaldo"), None),
        buildMatchForResult(4, "Ronaldo", "Zidane", "Maradona", "Messi", Some("Zidane"),  None),
        buildMatchForResult(5, "Ronaldo", "Zidane", "Maradona", "Messi", Some("Ronaldo"), None),
        buildMatchForResult(6, "Ronaldo", "Zidane", "Maradona", "Messi", Some("Ronaldo"), None)
      )

      val longestWinningStreaks = StatisticsService.determineLongestStreaks(matches, MatchResult.Result.Winner)

      longestWinningStreaks must haveSize(3)
      longestWinningStreaks.map(x => (x.player, x.record.value)) must contain(exactly(
        ("Ronaldo", 3), ("Ronaldo", 2), ("Zidane", 1)
      ))
    }

    "not be interrupted if the player did not play" in {
      val matches = List(
        buildMatchForResult(1, "Ronaldo", "Zidane", "Maradona", "Messi", Some("Ronaldo"), None),
        buildMatchForResult(2, "Ronaldo", "Zidane", "Maradona", "Messi", Some("Ronaldo"), None),
        buildMatchForResult(3, "Ronaldo", "Zidane", "Maradona", "Messi", Some("Ronaldo"), None),
        buildMatchForResult(4, "Pele",    "Zidane", "Maradona", "Messi", Some("Zidane"),  None),
        buildMatchForResult(5, "Ronaldo", "Zidane", "Maradona", "Messi", Some("Ronaldo"), None),
        buildMatchForResult(6, "Ronaldo", "Zidane", "Maradona", "Messi", Some("Ronaldo"), None)
      )

      val longestWinningStreaks = StatisticsService.determineLongestStreaks(matches, MatchResult.Result.Winner)

      longestWinningStreaks must haveSize(2)
      longestWinningStreaks.map(x => (x.player, x.record.value)) must contain(exactly(
        ("Ronaldo", 5), ("Zidane", 1)
      ))
    }

    "contain the latest streaks" in {
      val matches = List(
        buildMatchForResult(1, "Ronaldo", "Zidane", "Maradona", "Messi", Some("Ronaldo"), None),
        buildMatchForResult(2, "Ronaldo", "Zidane", "Maradona", "Messi", Some("Zidane"), None),
        buildMatchForResult(3, "Ronaldo", "Zidane", "Maradona", "Messi", Some("Maradona"), None),
        buildMatchForResult(4, "Ronaldo", "Zidane", "Maradona", "Messi", Some("Messi"), None)
      )

      val longestWinningStreaks = StatisticsService.determineLongestStreaks(matches, MatchResult.Result.Winner)

      longestWinningStreaks must haveSize(3)
      longestWinningStreaks.map(x => (x.player, x.record.value)) must contain(exactly(
        ("Messi", 1), ("Maradona", 1), ("Zidane", 1)
      ))
    }
  }

  "The highest score differences" should {
    "be empty when no matches have been played" in {
      val highestScoreDifferences = StatisticsService.determineHighestScoreDifferences(Seq())
      highestScoreDifferences must beEmpty
    }

    "ignore draws" in {
      val matches = List(
        buildMatchWithGame(1, "Ronaldo", "Zidane", "Maradona", "Messi", 0)
      )

      val highestScoreDifferences = StatisticsService.determineHighestScoreDifferences(matches)
      highestScoreDifferences must beEmpty
    }

    "contain the highest score differences" in {
      val matches = List(
        buildMatchWithGame(1, "Ronaldo", "Zidane", "Maradona", "Messi", 5),
        buildMatchWithGame(2, "Ronaldo", "Zidane", "Maradona", "Messi", 3),
        buildMatchWithGame(3, "Ronaldo", "Zidane", "Maradona", "Messi", 10),
        buildMatchWithGame(4, "Ronaldo", "Zidane", "Maradona", "Messi", 7)
      )

      val highestScoreDifferences = StatisticsService.determineHighestScoreDifferences(matches)

      val winners = Seq("Ronaldo", "Zidane")
      val losers = Seq("Maradona", "Messi")

      highestScoreDifferences must haveSize(3)
      highestScoreDifferences.map(x => (x.winners, x.losers, x.record.value)) must contain(exactly(
        (winners, losers, 10), (winners, losers, 7), (winners, losers,5)
      ))
    }

    "contain the latest score differences" in {
      val matches = List(
        buildMatchWithGame(1, "Maradona", "Messi", "Ronaldo", "Zidane", 10),
        buildMatchWithGame(2, "Ronaldo", "Zidane", "Maradona", "Messi", 10),
        buildMatchWithGame(3, "Ronaldo", "Zidane", "Maradona", "Messi", 10),
        buildMatchWithGame(4, "Ronaldo", "Zidane", "Maradona", "Messi", 10)
      )

      val highestScoreDifferences = StatisticsService.determineHighestScoreDifferences(matches)

      val oldWinners = Seq("Maradona", "Messi")
      val oldLosers = Seq("Ronaldo", "Zidane")

      highestScoreDifferences must haveSize(3)
      highestScoreDifferences.map(x => (x.winners, x.losers, x.record.value)) must not contain(
        (oldWinners, oldLosers, 10)
      )
    }
  }

  "The boundary partnerships" should {
    "be empty when no matches have been played" in {
      val weakestPartnerships = StatisticsService.determineBoundaryPartnerships(Seq(), Ordering[Double])
      weakestPartnerships must beEmpty
    }

    "contain the weakest partnerships" in {
      val games = List(
        buildGame(1, "Ronaldo", "Zidane", "Maradona", "Messi", 8),
        buildGame(2, "Ronaldo", "Zidane", "Maradona", "Messi", 4),
        buildGame(3, "Ronaldo", "Zidane", "Beckham",  "Pele",  6),
        buildGame(4, "Ronaldo", "Zidane", "Beckham",  "Pele",  10),
        buildGame(5, "Ronaldo", "Zidane", "Beckham",  "Messi", 10)
      )

      val weakestPartnerships = StatisticsService.determineBoundaryPartnerships(games, Ordering[Double])

      weakestPartnerships must haveSize(3)
      weakestPartnerships.map(x => (x.players, x.record.value)) must contain(exactly(
        (Seq("Beckham", "Messi"), -10), (Seq("Beckham", "Pele"), -8), (Seq("Maradona", "Messi"), -6)
      ))
    }
  }

  "The player nemesis" should {
    "be empty when no matches have been played" in {
      val playerNemesis = StatisticsService.determinePlayerNemesis(Seq())
      playerNemesis must beEmpty
    }

    "be the opponent with whom the player performs worst" in {
      val matches = List(
        buildMatchWithGame(1, "Ronaldo", "Zidane",   "Maradona", "Messi", 4),
        buildMatchWithGame(2, "Ronaldo", "Zidane",   "Beckham",  "Pele",  0),
        buildMatchWithGame(3, "Ronaldo", "Maradona", "Beckham", "Pele",   8)
      )

      val playerNemesis = StatisticsService.determinePlayerNemesis(matches)

      playerNemesis.keys must haveSize(6)
      playerNemesis("Ronaldo") must beEqualTo("Zidane")
    }
  }

  // ===== Builders =====

  private def buildPlayerElo(eloPos: Int, player: String, elo: Double): PlayerElo = PlayerElo(None, player, DateTime.now.plusMinutes(eloPos), 1L, 0.0, elo)

  private def buildMatchForResult(matchPos: Int, player1: String, player2: String, player3: String, player4: String, winner: Option[String], loser: Option[String]): MatchWithResults = {
    val foosMatch = Match(Some(matchPos), DateTime.now.plusMinutes(matchPos), "User", Match.Format.CompleteMatch)
    val playerResults = Seq(player1, player2, player3, player4).map { player =>
      val result = Some(player) match {
        case `winner` => MatchResult.Result.Winner
        case `loser`  => MatchResult.Result.Loser
        case _        => MatchResult.Result.NoResult
      }
      MatchResult(matchPos, player, result, 1, 2, 10)
    }

    MatchWithResults(foosMatch, playerResults)
  }

  private def buildMatchWithGame(matchPos: Int, leftPlayer1: String, leftPlayer2: String, rightPlayer1: String, rightPlayer2: String, goalDifference: Int): MatchWithGames = {
    val foosMatch = Match(Some(matchPos), DateTime.now.plusMinutes(matchPos), "User", Match.Format.CompleteMatch)
    val game = buildGame(matchPos, leftPlayer1, leftPlayer2, rightPlayer1, rightPlayer2, goalDifference)
    MatchWithGames(foosMatch, Seq(game))
  }

  private def buildGame(gamePos: Int, leftPlayer1: String, leftPlayer2: String, rightPlayer1: String, rightPlayer2: String, goalDifference: Int): Game = {
    val leftScore1 = if (goalDifference == 0) 0 else goalDifference / 2
    val leftScore2 = if (goalDifference == 0) 0 else if (goalDifference % 2 == 0) goalDifference / 2 else goalDifference / 2 + 1
    Game(Some(gamePos), gamePos, leftPlayer1, leftPlayer2, rightPlayer1, rightPlayer2, leftScore1, leftScore2, 0, 0)
  }

}
