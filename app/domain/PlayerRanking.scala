package domain

case class PlayerRanking(player: String, rank: Int, played: Int, won: Int, pseudoWon: Int, pseudoLost: Int, lost: Int, elo: Int)