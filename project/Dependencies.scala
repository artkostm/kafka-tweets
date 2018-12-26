import sbt._

object Dependencies {

  val versions = new {
    val alpakka    = "0.22"
    val jsoniter   = "0.37.6"
    val pureconfig = "0.10.1"
    val twitter4s  = "5.5"
    val akka = "2.5.13"

    val scalaTest  = "3.0.5"
    val scalaCheck = "1.14.0"
    val scalaMock  = "4.1.0"
    val randomDataGenerator = "2.6"
  }

  lazy val main = Seq(
    "com.danielasfregola"                   %% "twitter4s"             % versions.twitter4s,
    "com.github.pureconfig"                 %% "pureconfig"            % versions.pureconfig,
    "com.typesafe.akka"                     %% "akka-stream-kafka"     % versions.alpakka,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % versions.jsoniter % Compile,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % versions.jsoniter % Provided
  )

  lazy val testCommon = Seq(
    "org.scalatest"  %% "scalatest"                   % versions.scalaTest,
    "org.scalacheck" %% "scalacheck"                  % versions.scalaCheck,
    "org.scalamock"  %% "scalamock"                   % versions.scalaMock,
    "com.danielasfregola" %% "random-data-generator" % versions.randomDataGenerator
  )
  
  lazy val unitTests = (testCommon ++ Seq(
    "com.typesafe.akka" %% "akka-stream-testkit" % versions.akka
  )).map(_ % Test)

  lazy val itTests = (testCommon ++ Seq(
    "com.typesafe.akka" %% "akka-stream-testkit" % versions.akka,
    "com.typesafe.akka" %% "akka-stream-kafka-testkit" % "1.0-RC1"
  )).map(_ % IntegrationTest)
}
