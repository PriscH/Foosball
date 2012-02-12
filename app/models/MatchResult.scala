package models

import play.db.anorm._
import play.db.anorm.defaults._
import util.errors.DatabaseException._
import play.utils.Scala.MayErr
import java.util.Date
import util.errors.DatabaseException

case class MatchResult(
  match_id: Pk[Long], user_id: Pk[Long],
  rank: Integer, score: Integer
)

object MatchResult extends Magic[MatchResult] {
  implicit def mayErrToMatchResult(result: MayErr[SqlRequestError,MatchResult]): MatchResult = result.e match {
    case Left(error)        => throw DatabaseException(error)
    case Right(matchResult) => matchResult
  }

  def apply(match_id: Pk[Long], user_id: Pk[Long], rank: Integer) = new MatchResult(match_id, user_id, rank, 0)
}