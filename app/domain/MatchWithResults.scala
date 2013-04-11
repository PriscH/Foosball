package domain

import models._

/**
 * A Match along with the MatchResults for all its players
 */
case class MatchWithResults(foosMatch: Match, matchResults: Seq[MatchResult]) {
  
  def matchId: Long = foosMatch.id.get
  
  def resultString(): String = {
    // Has to be exactly one outright result
    val outrightResult = matchResults.find(_.outrightResult).get.resultString
    
    // May or may not have a pseudo result, but can't have more than one
    val resultString = matchResults.find(_.pseudoResult) match {
      case Some(pseudoResult) => outrightResult + " (and " + pseudoResult.resultString + ")"
      case None               => outrightResult
    }
    
    // Always more than one "nothing" result
    resultString + " in a match with " + matchResults.filter(_.noResult).map(_.player).mkString(", ")
  }
  
}