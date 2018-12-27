import sbt._
import Keys._
import sbtassembly._
import AssemblyKeys._

object Settings {

  def default(sv: String) = Seq(
    version := "0.1",
    scalaVersion := sv,
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", _ @_*) => MergeStrategy.discard
      case _                           => MergeStrategy.first
    }
  )
}
