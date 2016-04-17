import sbt._
import sbt.Keys._
import play.sbt.PlayImport._

object ApplicationBuild extends Build {

  val appName         = "Foosball"
  val appVersion      = "1.0-SNAPSHOT"
  val scalaAppVersion = "2.11.6"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    evolutions,
    specs2 % "test",
    "com.typesafe.play" %% "anorm" % "2.4.0"
  )

  val main = Project(appName, file(".")).enablePlugins(play.PlayScala).settings(
    version := appVersion,
    scalaVersion := scalaAppVersion,
    libraryDependencies ++= appDependencies
  )

}
