package domain

/**
 * A summary of a Player's wins/losses as well as their current rank according to their current elo.
 */
case class PlayerRanking(player: String, rank: Int, played: Int, won: Int, lost: Int, elo: Int, lastChange: Int)