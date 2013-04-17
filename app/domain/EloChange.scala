package domain

case class EloChange(elo: Int, change: Int)

object EloChange {
  implicit def fromTuple(t: (Int, Int)) = EloChange(t._1, t._2)
}
