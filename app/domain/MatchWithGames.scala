package domain

import models._

/**
 * A Match along with all its Games
 */
case class MatchWithGames(foosMatch: Match, games: Seq[Game]) {
  
  def capturedBy: String = foosMatch.capturedBy
  
}