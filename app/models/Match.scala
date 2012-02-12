package models

import play.db.anorm._
import play.db.anorm.defaults._
import play.utils.Scala.MayErr
import java.util.Date
import util.errors.DatabaseException

case class Match(
  id: Pk[Long],
  match_type: String, created_by: Long, created_date: Date
)

object Match extends Magic[Match] {
  def apply(matchType: MatchType) = {
    new Match(NotAssigned, matchType.toString, 0L, new Date())
  }

  implicit def mayErrToMatch(result: MayErr[SqlRequestError,Match]): Match = result.e match {
    case Left(error)        => throw DatabaseException(error)
    case Right(storedMatch) => storedMatch
  }
}

sealed abstract class MatchType
case object TwoOnTwo extends MatchType {
  override def toString = "TwoOnTwo"
}