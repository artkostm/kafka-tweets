import sbt._

object Dependencies {

  val versions = new {
    val alpakka    = "0.22"
    val jsoniter   = "0.37.6"
    val pureconfig = "0.10.1"
    val twitter4s  = "5.5"
    val akka       = "2.5.13"
    val logback    = "1.2.3"
    val spark      = "2.3.0"
    val lz4        = "1.3.0"

    val scalaTest           = "3.0.5"
    val scalaCheck          = "1.14.0"
    val scalaMock           = "4.1.0"
    val randomDataGenerator = "2.6"
    val alpakka_testkit     = "1.0-RC1"
    val embeddedKafka       = "2.0.0"
  }

  lazy val excludeJpountz = ExclusionRule(organization = "net.jpountz.lz4", name = "lz4")

  lazy val spark = Seq(
    "org.apache.spark" %% "spark-core" % versions.spark % Provided,
    "org.apache.spark" %% "spark-sql"  % versions.spark % Provided,
    "org.apache.spark"      %% "spark-sql-kafka-0-10" % versions.spark excludeAll (excludeJpountz),
  )

  lazy val mainPublisher = Seq(
    "com.danielasfregola"                   %% "twitter4s"             % versions.twitter4s,
    "com.github.pureconfig"                 %% "pureconfig"            % versions.pureconfig,
    "com.typesafe.akka"                     %% "akka-stream-kafka"     % versions.alpakka,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % versions.jsoniter % Compile,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % versions.jsoniter % Provided,
    "com.typesafe.akka"                     %% "akka-slf4j"            % versions.akka,
    "ch.qos.logback"                        % "logback-classic"        % versions.logback
  )

  lazy val mainBatching = spark ++ Seq(
    "com.github.pureconfig" %% "pureconfig"           % versions.pureconfig,
    "org.apache.spark"      %% "spark-core"           % versions.spark % Provided,
    "org.apache.spark"      %% "spark-sql"            % versions.spark % Provided
  )

  lazy val mainStreaming = spark ++ Seq(
    "org.apache.spark" %% "spark-streaming" % versions.spark % Provided,
    "com.github.pureconfig" %% "pureconfig"           % versions.pureconfig,
    "org.apache.spark"      %% "spark-core"           % versions.spark % Provided,
    "org.apache.spark"      %% "spark-sql"            % versions.spark % Provided, 
    "org.apache.spark" %% "spark-streaming-kafka-0-10" % versions.spark
  )

  private lazy val testCommon = Seq(
    "org.scalatest"       %% "scalatest"             % versions.scalaTest,
    "org.scalacheck"      %% "scalacheck"            % versions.scalaCheck,
    "org.scalamock"       %% "scalamock"             % versions.scalaMock,
    "com.danielasfregola" %% "random-data-generator" % versions.randomDataGenerator
  )

  lazy val unitTestsPublisher = (testCommon ++ Seq(
    "com.typesafe.akka" %% "akka-stream-testkit" % versions.akka
  )).map(_ % Test)

  lazy val itTestsPublisher = (testCommon ++ Seq(
    "com.typesafe.akka" %% "akka-stream-testkit"       % versions.akka,
    "com.typesafe.akka" %% "akka-stream-kafka-testkit" % versions.alpakka_testkit
  )).map(_ % IntegrationTest)

  lazy val unitTestsBatching = (testCommon ++ Seq(
  )).map(_ % Test)

  lazy val itTestsBatching = (testCommon ++ Seq(
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"      % versions.jsoniter,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros"    % versions.jsoniter,
    "com.danielasfregola"                   %% "twitter4s"                % versions.twitter4s,
    "net.manub"                             %% "scalatest-embedded-kafka" % versions.embeddedKafka,
    "com.typesafe.akka"                     %% "akka-stream-testkit"      % versions.akka
  )).map(_ % IntegrationTest)

  lazy val itTestsStreaming = itTestsBatching
  
  lazy val common = Seq(
    "org.apache.spark" %% "spark-catalyst" % versions.spark % Provided,
    "org.apache.spark"      %% "spark-sql"            % versions.spark % Provided intransitive()
  )
}
