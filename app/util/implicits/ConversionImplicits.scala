package util.implicits

import org.joda.time.{DateTime, LocalDate}
import scala.language.implicitConversions

object ConversionImplicits {

  implicit def dateTimeToLocalDate(dateTime: DateTime): LocalDate = dateTime.toLocalDate

}
