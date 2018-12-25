import sbt._
import Keys._
import sbtassembly._
import AssemblyKeys._

object Settings {
  lazy val default = Seq(
    name := "kafka-tweets",
    version := "0.1",
    scalaVersion := "2.12.8",
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", _ @_*) => MergeStrategy.discard
      case _                           => MergeStrategy.first
    }
  )
}
