package services

import models.{MatchResult, Match, User}

object PlayerService {

  // ===== Interface =====

  def findMostRecentOpponents(implicit user: User): Seq[String] = {
    Match.findRecentForPlayer(user.name, 1).headOption match {
      case Some(foosMatch) => MatchResult.findByMatch(foosMatch.id.get).map(_.player)
      case None            => Nil
    }
  }
}
