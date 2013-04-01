package domain

import models._

case class MatchWithResults(foosMatch: Match, matchResults: Seq[MatchResult]) {
  
  def matchId(): Long = {
    foosMatch.id.get
  }
  
  def resultString(): String = {
    val outrightResult = matchResults.filter(_.outrightResult).head.resultString
    val resultString = matchResults.filter(_.pseudoResult).headOption match {
      case Some(pseudoResult) => outrightResult + " (and " + pseudoResult.resultString + ")"
      case None               => outrightResult
    }
    
    resultString + " in a match with " + matchResults.filter(_.noResult).map(_.player).mkString(", ")
  }
  
}