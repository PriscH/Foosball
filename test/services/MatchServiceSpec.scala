package services

import org.specs2.mutable._

class MatchServiceSpec extends Specification {

  "An outright Winner" should {
    "result from 2-0, 2-0, 2-0" in {
      MatchService.playerResult(6, List(6, 2, 2, 2)) mustEqual "Winner" 
    }
    
    "result from 2-1, 2-0, 2-0" in {
      MatchService.playerResult(6, List(6, 3, 2, 2)) mustEqual "Winner" 
    }
    
    "result from 2-1, 2-1, 2-0" in {
      MatchService.playerResult(6, List(6, 3, 3, 2)) mustEqual "Winner" 
    }
    
    "result from 2-1, 2-1, 2-1" in {
      MatchService.playerResult(6, List(6, 3, 3, 3)) mustEqual "Winner" 
    }
  }
  
  "A Pseudo-Winner" should {
    "result from 2-0 2-0 1-2" in {
      MatchService.playerResult(5, List(5, 4, 4, 1)) mustEqual "Pseudo-Winner" 
    }
    
    "result from 2-0 1-2 1-2" in {
      MatchService.playerResult(4, List(4, 6, 3, 3)) mustEqual "Pseudo-Winner" 
    }
  }
  
  "A Pseudo-Loser" should {
    "result from 0-2 0-2 2-1" in {
      MatchService.playerResult(2, List(2, 3, 3, 6)) mustEqual "Pseudo-Loser" 
    }
    
    "result from 2-1 2-1 0-2" in {
      MatchService.playerResult(4, List(4, 5, 5, 2)) mustEqual "Pseudo-Loser" 
    }
  }
  
  "A Loser" should {
    "result from 0-2 0-2 0-2" in {
      MatchService.playerResult(0, List(0, 4, 4, 4)) mustEqual "Loser" 
    }
    
    "result from 1-2 0-2 0-2" in {
      MatchService.playerResult(1, List(1, 5, 4, 4)) mustEqual "Loser" 
    }
    
    "result from 1-2 1-2 0-2" in {
      MatchService.playerResult(2, List(2, 5, 5, 4)) mustEqual "Loser" 
    }
    
    "result from 1-2 1-2 1-2" in {
      MatchService.playerResult(3, List(3, 5, 5, 5)) mustEqual "Loser" 
    }
  }
  
}