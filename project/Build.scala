import sbt._
import sbt.Keys._

object Build extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = plugins.JvmPlugin

  override lazy val projectSettings = List(
    organization := "de.knutwalker",
       startYear := Some(2015),
    scalaVersion := "2.11.7",
            name := s"wdcm-${thisProject.value.id}",
   scalacOptions := List(
     "-deprecation",
     "-encoding", "UTF-8",
     "-feature",
     "-language:existentials",
     "-language:higherKinds",
     "-language:implicitConversions",
     "-language:postfixOps",
     "-target:jvm-1.8",
     "-unchecked",
     "-Xcheckinit",
     "-Xfatal-warnings",
     "-Xfuture",
     "-Xlint:_",
     "-Yclosure-elim",
     "-Yconst-opt",
     "-Yno-adapted-args",
     "-Ywarn-adapted-args",
     "-Ywarn-inaccessible",
     "-Ywarn-infer-any",
     "-Ywarn-nullary-override",
     "-Ywarn-nullary-unit",
     "-Ywarn-numeric-widen"
   ))
}
