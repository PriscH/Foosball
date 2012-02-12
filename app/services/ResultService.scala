package services

import util.errors.{DatabaseException, InternalException}
import models._

object ResultService {

  def recordQuick(playerResults: Seq[(String, Option[ResultType])]) {
    val playedMatch: Match = Match.create(Match(TwoOnTwo))

    // IN SQL clause won't work: http://groups.google.com/group/play-framework/browse_thread/thread/04d16f99adafbb95
    val players = playerResults.map(resultTuple =>
      User.findByName(resultTuple._1) match {
        case Some(user) => MatchResult.create(createResultFor(playedMatch, user, resultTuple._2))
        case None       => throw InternalException("Player does not exist")
      }
    )
  }

  private def createResultFor(playedMatch: Match, user: User, resultType: Option[ResultType]) = {
    resultType match {
      case Some(WIN)  => MatchResult(playedMatch.id, user.id, 1)
      case Some(LOSS) => MatchResult(playedMatch.id, user.id, 4)
      case None       => MatchResult(playedMatch.id, user.id, 2)
    }
  }
}