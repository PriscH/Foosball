package domain

import scala.language.implicitConversions

case class EloWithChange(elo: Double, change: Double)

object EloWithChange {
  implicit def fromTuple(t: (Double, Double)) = EloWithChange(t._1, t._2)
}
