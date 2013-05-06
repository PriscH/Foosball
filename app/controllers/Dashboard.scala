package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.libs.json.JsValue

import anorm.NotAssigned

import domain._
import models._
import views._
import services._
import util.security._
import util.html.HtmlExtension._

object Dashboard extends Controller with Secured {
  
  val RecentMatchCount = 3
  val FlashSuccess = "success"
  val FlashConflictingMatch = "conflictingMatchId"
  
  // ===== Forms =====
  
  val captureMatchForm = Form(
    "results" -> nonEmptyText
  ) 

  // ===== Actions =====
  
  def show = SecuredAction { implicit user => implicit request => {     
    val recentMatches = Match.findRecentForPlayer(user.name, RecentMatchCount).map(foosMatch => MatchWithResults(foosMatch, MatchResult.findByMatch(foosMatch.id.get)))  
    val playerRankings = RankingService.loadCurrentRankings.sortBy(_.rank)
    
    if (flash.get(FlashConflictingMatch).isDefined) {
      val conflictingMatch = MatchService.findMatchWithGames(flash.get(FlashConflictingMatch).get.toLong)
      Ok(html.dashboard.index(User.all, recentMatches, playerRankings, conflictingMatch))
    } else {    
      Ok(html.dashboard.index(User.all, recentMatches, playerRankings))
    }
  }}
  
  def refresh = SecuredAction { implicit user => implicit request => {
    val recentMatches = Match.findRecentForPlayer(user.name, RecentMatchCount).map(foosMatch => MatchWithResults(foosMatch, MatchResult.findByMatch(foosMatch.id.get)))  
    val playerRankings = RankingService.loadCurrentRankings.sortBy(_.rank)
    
    Ok(Json.obj(
      "rankings"      -> html.tags.rankingTable(playerRankings),
      "recentMatches" -> html.tags.recentMatches(recentMatches)
    ))
  }}
  
  /**
   * With force == true will capture the Match even if an identical Match has been captured recently
   */
  def captureMatch(force: Boolean) = SecuredAction { implicit user => implicit request => {
    val resultsJson = captureMatchForm.bindFromRequest.get
    val results = Json.parse(resultsJson)
    val games = parseGames(results)
    
    val conflictingMatch = MatchService.findRecentConflictingMatch(games)
    if (force || conflictingMatch.isEmpty) {
      MatchService.captureMatch(games)           
      Redirect(routes.Dashboard.show).flashing(FlashSuccess -> Messages("match.capture.success"))
    } else {
      Redirect(routes.Dashboard.show).flashing(FlashConflictingMatch -> conflictingMatch.get.id.get.toString)
    }
  }}  
  
  // ===== Helpers =====
  
  // TODO: Should probably do this with Reads
  private def parseGames(results: JsValue): Seq[Game] = for (index <- 0 until 3) yield {
    val winner1 = ((results \ "games")(index) \ "winners")(0).as[String]
    val winner2 = ((results \ "games")(index) \ "winners")(1).as[String]
    
    val loser1 = ((results \ "games")(index) \ "losers")(0).as[String]
    val loser2 = ((results \ "games")(index) \ "losers")(1).as[String]
    
    val score = (((results \ "games")(index)) \ "score").as[String]
        
    Game(NotAssigned, 0, winner1, winner2, loser1, loser2, Game.Score.withName(score))
  } 
}