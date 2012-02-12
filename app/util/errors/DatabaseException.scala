package util.errors

import play.db.anorm.SqlRequestError

case class DatabaseException(sqlError: SqlRequestError) extends RuntimeException