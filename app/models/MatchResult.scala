package models

import play.db.anorm._
import play.db.anorm.defaults._

case class MatchResult(
  match_id: Pk[Long], user_id: Pk[Long],
  rank: Integer, score: Integer
)

object MatchResult extends Magic[MatchResult]