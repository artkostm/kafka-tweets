package by.artsiom.bigdata201.job

import java.io.File
import java.nio.file.Files
import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.TestKit
import by.artsiom.bigdata201.tweet._
import com.danielasfregola.twitter4s.entities.Tweet
import com.github.plokhotnyuk.jsoniter_scala.core.writeToArray
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.commons.io.FileUtils
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.spark.sql.SparkSession
import org.scalatest.FlatSpecLike

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.collection.JavaConverters._

class JobIntegTest
    extends TestKit(ActorSystem("job_integ_test"))
    with FlatSpecLike
    with EmbeddedKafka {
  import JobIntegTest._

  implicit val mat                 = ActorMaterializer()
  implicit val kafkaSerializer     = new ByteArraySerializer()
  implicit val tweetJsonValueCodec = JsonCodecMaker.make[Tweet](CodecMakerConfig())

  val kafkaConfig = EmbeddedKafkaConfig()

  def withConfig(kafkaConf: EmbeddedKafkaConfig)(test: (String, String) => Unit): Unit = {
    val tmpDir1 = FileUtils.getTempDirectoryPath + File.separator + TempDirectoryPrefix + UUID
      .randomUUID()
      .toString
    val tmpDir2 = FileUtils.getTempDirectoryPath + File.separator + TempDirectoryPrefix + UUID
      .randomUUID()
      .toString
    try test(tmpDir1, tmpDir2)
    finally {
      FileUtils.deleteQuietly(new File(tmpDir1))
      FileUtils.deleteQuietly(new File(tmpDir2))
    }
  }

  it should "successfully create parquet files from kafka messages" in withConfig(kafkaConfig) {
    (topicDataDir, countsDir) =>
      withRunningKafkaOnFoundPort(kafkaConfig) { implicit kafkaConfigWithPorts =>
        val messagesPublished = Source(List(TweetGenerator.rendomTweet(Hashtag)))
          .runWith(
            Sink.foreach(
              tweet =>
                publishToKafka(Topic,
                               (s"${tweet.user.map(_.name).getOrElse()}:$Hashtag").getBytes,
                               writeToArray(tweet))
            )
          )

        assert(Await.result(messagesPublished, 5 seconds) == Done)

        val appconfig = AppConfig(KafkaConfig(bootstrapServers =
                                                s"localhost:${kafkaConfigWithPorts.kafkaPort}",
                                              topic = Topic,
                                              checkpointLocation = ""),
                                  OutputConfig(topicDataDir, countsDir))

        Main.runJob(
          appconfig,
          SparkSession.builder.appName("batching-integ-test").master("local").getOrCreate()
        )

        val topicFiles  = Files.list(new File(topicDataDir).toPath).iterator().asScala
        val countsFiles = Files.list(new File(countsDir).toPath).iterator().asScala

        assert(topicFiles.exists(_.getFileName.toString == "_SUCCESS"))
        assert(countsFiles.exists(_.getFileName.toString == "_SUCCESS"))
      }
  }
}

object JobIntegTest {
  val Topic               = "TestTopic"
  val TempDirectoryPrefix = "tmp-integ-tests-"
  val Hashtag             = "IntegTest"
}
