package services

import org.specs2.mutable._

import models._

class MatchServiceSpec extends Specification {

  "An outright Winner" should {
    "result from 2-0, 2-0, 2-0" in {
      MatchService.calculateResult(6, List(6, 2, 2, 2)) mustEqual MatchResult.Result.Winner 
    }
    
    "result from 2-1, 2-0, 2-0" in {
      MatchService.calculateResult(6, List(6, 3, 3, 2)) mustEqual MatchResult.Result.Winner 
    }
    
    "result from 2-1, 2-1, 2-0" in {
      MatchService.calculateResult(6, List(6, 4, 3, 3)) mustEqual MatchResult.Result.Winner  
    }
    
    "result from 2-1, 2-1, 2-1" in {
      MatchService.calculateResult(6, List(6, 4, 4, 4)) mustEqual MatchResult.Result.Winner 
    }
  }
  
  "A Pseudo-Winner" should {
    "result from 2-0 2-0 1-2" in {
      MatchService.calculateResult(5, List(5, 4, 4, 1)) mustEqual MatchResult.Result.PseudoWinner  
    }
    
    "result from 2-0 1-2 1-2" in {
      MatchService.calculateResult(4, List(4, 6, 3, 3)) mustEqual MatchResult.Result.PseudoWinner  
    }
  }
  
  "A Pseudo-Loser" should {
    "result from 0-2 0-2 2-1" in {
      MatchService.calculateResult(2, List(2, 3, 3, 6)) mustEqual MatchResult.Result.PseudoLoser 
    }
    
    "result from 2-1 2-1 0-2" in {
      MatchService.calculateResult(4, List(4, 5, 5, 2)) mustEqual MatchResult.Result.PseudoLoser 
    }
  }
  
  "A Loser" should {
    "result from 0-2 0-2 0-2" in {
      MatchService.calculateResult(0, List(0, 4, 4, 4)) mustEqual MatchResult.Result.Loser 
    }
    
    "result from 1-2 0-2 0-2" in {
      MatchService.calculateResult(1, List(1, 5, 4, 4)) mustEqual MatchResult.Result.Loser  
    }
    
    "result from 1-2 1-2 0-2" in {
      MatchService.calculateResult(2, List(2, 5, 5, 4)) mustEqual MatchResult.Result.Loser  
    }
    
    "result from 1-2 1-2 1-2" in {
      MatchService.calculateResult(3, List(3, 5, 5, 5)) mustEqual MatchResult.Result.Loser 
    }
  }
  
  "Nothing" should {
    "result from 2-0, 2-0, 2-0" in {
      MatchService.calculateResult(2, List(6, 2, 2, 2)) mustEqual MatchResult.Result.NoResult 
    }
    
    "result from 2-1, 2-0, 2-0" in {
      MatchService.calculateResult(3, List(6, 3, 3, 2)) mustEqual MatchResult.Result.NoResult 
    }
    
    "result from 2-1, 2-1, 2-0" in {
      MatchService.calculateResult(3, List(6, 4, 3, 3)) mustEqual MatchResult.Result.NoResult 
    }
    
    "result from 2-1, 2-1, 2-1" in {
      MatchService.calculateResult(4, List(6, 4, 4, 4)) mustEqual MatchResult.Result.NoResult 
    }
    
    "result from 0-2 0-2 0-2" in {
      MatchService.calculateResult(4, List(0, 4, 4, 4)) mustEqual MatchResult.Result.NoResult 
    }
    
    "result from 1-2 0-2 0-2" in {
      MatchService.calculateResult(4, List(1, 5, 4, 4)) mustEqual MatchResult.Result.NoResult 
    }
    
    "result from 1-2 1-2 0-2" in {
      MatchService.calculateResult(5, List(2, 5, 5, 4)) mustEqual MatchResult.Result.NoResult
    }
    
    "result from 1-2 1-2 1-2" in {
      MatchService.calculateResult(5, List(3, 5, 5, 5)) mustEqual MatchResult.Result.NoResult 
    }
  }
}