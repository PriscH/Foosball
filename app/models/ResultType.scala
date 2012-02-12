package models

sealed abstract class ResultType
case object WIN extends ResultType
case object LOSS extends ResultType