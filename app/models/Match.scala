package models

import play.db.anorm._
import play.db.anorm.defaults._
import org.joda.time.DateTime

case class Match(
  id: Pk[Long],
  match_type: String, created_by: Long, created_date: DateTime
)

object Match extends Magic[Match]