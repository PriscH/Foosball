package util.db

import org.joda.time._
import org.joda.time.format._
import anorm._

/**
 * Implicit conversions and extractors for Anorm types not supported by default
 */
object AnormExtension {
      
  /*
   * Allows JodaTime to be used with Anorm.
   * Credit and reference: http://stackoverflow.com/questions/11388301/joda-datetime-field-on-play-framework-2-0s-anorm
   */
  val dateFormatGeneration: DateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmssSS");

  implicit def rowToDateTime: Column[DateTime] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case ts: java.sql.Timestamp => Right(new DateTime(ts.getTime))
      case d: java.sql.Date       => Right(new DateTime(d.getTime))
      case str: java.lang.String  => Right(dateFormatGeneration.parseDateTime(str))  
      case _                      => Left(TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass + " to DateTime for column " + qualified))
    }
  }

  implicit val dateTimeToStatement = new ToStatement[DateTime] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: DateTime): Unit = {
      s.setTimestamp(index, new java.sql.Timestamp(aValue.withMillisOfSecond(0).getMillis()) )
    }
  }
  
  /*
   * Allows scala's BigDecimal to be used as Doubles with Anorm
   */
  implicit def rowToDouble: Column[Double] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    
    value match {
      case bd: BigDecimal           => Right(bd.toDouble)
      case bd: java.math.BigDecimal => Right(BigDecimal(bd).toDouble)
      case int: Int                 => Right(int.toDouble)
      case long: Long               => Right(long.toDouble)
      case float: Float             => Right(float.toDouble)
      case double: Double           => Right(double)
      case _                        => Left(TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass + " to Double for column " + qualified))
    }
  }
  
  implicit val doubleToStatement = new ToStatement[Double] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: Double): Unit = {
      s.setDouble(index, aValue)
    }
  }
}