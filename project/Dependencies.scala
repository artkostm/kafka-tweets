import sbt._

object Dependencies {

  val versions = new {
    val alpakka    = "0.22"
    val jsoniter   = "0.37.6"
    val pureconfig = "0.10.1"
    val twitter4s  = "5.5"
  }

  lazy val main = Seq(
    "com.danielasfregola"                   %% "twitter4s"             % versions.twitter4s,
    "com.github.pureconfig"                 %% "pureconfig"            % versions.pureconfig,
    "com.typesafe.akka"                     %% "akka-stream-kafka"     % versions.alpakka,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % versions.jsoniter % Compile,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % versions.jsoniter % Provided
  )
}
