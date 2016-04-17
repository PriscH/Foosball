package domain

import org.joda.time.DateTime

case class Statistics(
  highestElos: Seq[SinglePlayerRecord],
  lowestElos: Seq[SinglePlayerRecord],
  longestWinningStreaks: Seq[SinglePlayerRecord],
  longestLosingStreaks: Seq[SinglePlayerRecord],
  highestScoreDifferences: Seq[MatchRecord],
  strongestPartnerships: Seq[PartnershipRecord],
  weakestPartnerships: Seq[PartnershipRecord],
  playerNemesis: Map[String, Option[String]]
)

case class Record(value: Int, date: Option[DateTime] = Option.empty)

case class SinglePlayerRecord(player: String, record: Record)

case class PartnershipRecord(players: Seq[String], record: Record)

case class MatchRecord(winners: Seq[String], losers: Seq[String], record: Record)