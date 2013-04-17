package domain

case class EloWithChange(elo: Int, change: Int)

object EloWithChange {
  implicit def fromTuple(t: (Int, Int)) = EloWithChange(t._1, t._2)
}
