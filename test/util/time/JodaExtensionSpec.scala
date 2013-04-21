package util.time

import org.specs2.mutable._
import org.joda.time._

class JodaExtensionSpec extends Specification {

  "The formatted duration" should {
    "display as just occurred" in {
      val period = new Period(new DateTime(), new DateTime())
      JodaExtension.formatElapsedTime(period) mustEqual "that just occurred"
    }
    
    "display in seconds" in {
      val period = new Period(new DateTime(), new DateTime().plusSeconds(3))
      JodaExtension.formatElapsedTime(period) mustEqual "3 seconds ago"
    }
    
    "display in minutes" in {
      val period = new Period(new DateTime(), new DateTime().plusMinutes(14))
      JodaExtension.formatElapsedTime(period) mustEqual "14 minutes ago"
    }
    
    "display in hours" in {
      val period = new Period(new DateTime(), new DateTime().plusHours(10))
      JodaExtension.formatElapsedTime(period) mustEqual "10 hours ago"
    }
    
    "display in days" in {
      val period = new Period(new DateTime(), new DateTime().plusDays(25))
      JodaExtension.formatElapsedTime(period) mustEqual "25 days ago"
    }
        
    "display as a long time ago" in {
      val period = new Period(new DateTime(), new DateTime().plusMonths(1))
      JodaExtension.formatElapsedTime(period) mustEqual "a long time ago"
    }
  }
  
}