package util.time

import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Period

object JodaExtension {
  
  val SecondDuration = new Duration(1000L) 
  val MinuteDuration = new Duration(1000L * 60)
  val HourDuration   = new Duration(1000L * 60 * 60)
  val DayDuration    = new Duration(1000L * 60 * 60 * 24)

  def formatElapsedTime(period: Period): String = {
    // JodaTime doesn't support Durations that span months
    if (period.getMonths() > 0)                        "a long time ago"
    else {
      val duration = period.toStandardDuration()
      
      if (duration.isShorterThan(SecondDuration))      "that just occurred"   
      else if (duration.isShorterThan(MinuteDuration)) duration.getStandardSeconds() + " seconds ago"
      else if (duration.isShorterThan(HourDuration))   duration.getStandardMinutes() + " minutes ago"
      else if (duration.isShorterThan(DayDuration))    duration.getStandardHours() + " hours ago"
      else                                             duration.getStandardDays() + " days ago"   
    }
  }
}