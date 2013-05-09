package domain

import scala.language.implicitConversions

case class EloWithChange(elo: Double, change: Double) {
  
  val displayElo = Math.round(elo).toInt
  val displayChange = Math.round(change).toInt
  
}

object EloWithChange {
  implicit def fromTuple(t: (Double, Double)) = EloWithChange(t._1, t._2)
}
