package util.implicits

import org.joda.time.{DateMidnight, DateTime}

object ConversionImplicits {

  implicit def dateTimeToDateMidnight(dateTime: DateTime): DateMidnight = dateTime.toDateMidnight

}
