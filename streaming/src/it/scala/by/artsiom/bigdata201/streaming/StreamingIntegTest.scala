package by.artsiom.bigdata201.streaming

import java.io.File
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
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringDeserializer}
import org.apache.spark.sql.SparkSession
import org.scalatest.FlatSpecLike

import scala.concurrent.Await
import scala.concurrent.duration._

class StreamingIntegTest extends TestKit(ActorSystem("streamint_test"))
  with FlatSpecLike
  with EmbeddedKafka {

  import StreamingIntegTest._

  implicit val mat             = ActorMaterializer()
  implicit val kafkaSerializer = new ByteArraySerializer()
  implicit val kafkaDeserializer = new StringDeserializer()
  implicit val tweetJsonValueCodec = JsonCodecMaker.make[Tweet](CodecMakerConfig())
  implicit val dispatcher = system.dispatcher

  val kafkaConfig = EmbeddedKafkaConfig()

  def withConfig(kafkaConf: EmbeddedKafkaConfig)(test: String => Unit): Unit = {
    val tmpDir1 = FileUtils.getTempDirectoryPath + File.separator + TempDirectoryPrefix + UUID
      .randomUUID()
      .toString
    try test(tmpDir1)
    finally {
      FileUtils.deleteQuietly(new File(tmpDir1))
    }
  }

  it should "successfully stream hashtag counts to Kafka" in withConfig(kafkaConfig) { checkpointLocation =>
    withRunningKafkaOnFoundPort(kafkaConfig) { implicit kafkaConfigWithPorts =>
      val messagesPublished = Source(List(TweetGenerator.rendomTweet(Hashtag)))
        .runWith(
          Sink.foreach(
            tweet =>
              publishToKafka(TopicToRead,
                (s"${tweet.user.map(_.name).getOrElse()}:$Hashtag").getBytes,
                writeToArray(tweet))
          )
        )

      assert(Await.result(messagesPublished, 10 seconds) == Done)

      val spark = SparkSession.builder.appName("streaming-integ-test").master("local").getOrCreate()
      system.scheduler.scheduleOnce(20 seconds) {
        spark.streams.active.foreach(_.stop())
      }

      val kafkaConfig = KafkaConfig(s"localhost:${kafkaConfigWithPorts.kafkaPort}", TopicToRead, "earliest", checkpointLocation)
      val appconfig = AppConfig(kafkaConfig, kafkaConfig.copy(topic = TopicToWrite), StreamingConfig("2 seconds", "2 seconds", "2 seconds"))

      Main.runJob(
        appconfig,
        spark
      )

      val (key, value) = consumeFirstKeyedMessageFrom(TopicToWrite)

      assert(value == "1")
      assert(key == Hashtag)
    }
  }
}

object StreamingIntegTest {
  val TopicToRead         = "StreamingTopicTest1"
  val TopicToWrite        = "StreamingTopicTest2"
  val TempDirectoryPrefix = "tmp-streaming-"
  val Hashtag             = "IntegTest"
}