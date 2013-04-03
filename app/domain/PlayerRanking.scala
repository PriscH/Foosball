package domain

case class PlayerRanking(name: String, played: Int, won: Int, pseudoWon: Int, pseuoLost: Int, lost: Int, elo: Int)