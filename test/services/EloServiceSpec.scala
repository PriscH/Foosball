package services

import org.specs2.mutable._

import models._

class EloServiceSpec extends Specification {

  "The expected result" should {
    "be large against an opponent with a smaller elo" in {
      EloService.estimateScoreVersus(1200, 1000) must beCloseTo(0.75975, 0.0001)
    }
    
    "be 0.5 against an opponent with the exact same elo" in {
      EloService.estimateScoreVersus(1200, 1200) must beCloseTo(0.5, 0.0001) 
    }
    
    "be small against an opponent with a larger elo" in {
      EloService.estimateScoreVersus(1200, 1400) must beCloseTo(0.24025, 0.0001)  
    }
  }
}