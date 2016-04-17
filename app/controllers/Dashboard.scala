package controllers

import _root_.util.html.HtmlExtension._
import _root_.util.security._
import models._
import play.api.Play.current
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.{JsNumber, _}
import play.api.mvc._
import services._
import views._

object Dashboard extends Controller with Secured {
  
  val FlashSuccess = "success"
  val FlashConflictingMatch = "conflictingMatchId"
  
  // ===== Forms =====
  
  val captureMatchForm = Form(
    tuple(
      "results" -> nonEmptyText,
      "force"   -> boolean
    )
  ) 

  // ===== Actions =====
  
  def show = SecuredAction { implicit user => implicit request => {     
    val recentMatches = MatchService.findRecentMatchesForPlayer(user.name)
    val playerRankings = RankingService.loadCurrentRankings.sortBy(_.rank)
    
    if (request.flash.get(FlashConflictingMatch).isDefined) {
      val conflictingMatch = MatchService.findMatchWithGames(request.flash.get(FlashConflictingMatch).get.toLong)
      val conflictingMatchJson = conflictingMatch map(foosMatch => toJson(foosMatch.games))
      Ok(html.dashboard.index(User.all, recentMatches, playerRankings, conflictingMatch, conflictingMatchJson))
    } else {    
      Ok(html.dashboard.index(User.all, recentMatches, playerRankings))
    }
  }}
  
  def refresh = SecuredAction { implicit user => implicit request => {
    val recentMatches = MatchService.findRecentMatchesForPlayer(user.name)  
    val playerRankings = RankingService.loadCurrentRankings.sortBy(_.rank)
    
    Ok(Json.obj(
      "rankings"      -> html.tags.rankingTable(playerRankings),
      "recentMatches" -> html.tags.recentMatches(recentMatches)
    ))
  }}
  
  def captureMatch() = SecuredAction { implicit user => implicit request => {    
    val (resultsJson, force) = captureMatchForm.bindFromRequest.get
    val results = Json.parse(resultsJson)
    val games = parseGames(results)
    
    // With force == true will capture the Match even if an identical Match has been captured recently
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
    val leftPlayer1 = (((results \ "games")(index) \ "sides")(0) \ "player1").as[String]
    val leftPlayer2 = (((results \ "games")(index) \ "sides")(0) \ "player2").as[String]

    val rightPlayer1 = (((results \ "games")(index) \ "sides")(1) \ "player1").as[String]
    val rightPlayer2 = (((results \ "games")(index) \ "sides")(1) \ "player2").as[String]

    val leftScore1 = (((results \ "games")(index) \ "sides")(0) \ "score1").as[Int]
    val leftScore2 = (((results \ "games")(index) \ "sides")(0) \ "score2").as[Int]

    val rightScore1 = (((results \ "games")(index) \ "sides")(1) \ "score1").as[Int]
    val rightScore2 = (((results \ "games")(index) \ "sides")(1) \ "score2").as[Int]
        
    Game(None, 0, leftPlayer1, leftPlayer2, rightPlayer1, rightPlayer2, leftScore1, leftScore2, rightScore1, rightScore2)
  }

  // TODO: This is just the inverse of parseGames
  private def toJson(games: Seq[Game]): JsValue = Json.toJson( Map(
    "games" -> (games map { game =>
      Json.toJson( Map (
        "sides" -> List(
          Json.toJson( Map (
            "player1" -> JsString(game.leftPlayer1),
            "player2" -> JsString(game.leftPlayer2),
            "score1"  -> JsNumber(game.leftScore1),
            "score2"  -> JsNumber(game.leftScore2)
          )),
          Json.toJson( Map (
            "player1" -> JsString(game.rightPlayer1),
            "player2" -> JsString(game.rightPlayer2),
            "score1"  -> JsNumber(game.rightScore1),
            "score2"  -> JsNumber(game.rightScore2)
          ))
        )
      ))
    })
  ))
}