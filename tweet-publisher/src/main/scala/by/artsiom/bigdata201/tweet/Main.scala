package by.artsiom.bigdata201.tweet

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import by.artsiom.bigdata201.tweet.config.AppConfig
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}
import org.apache.kafka.common.serialization.ByteArraySerializer
import pureconfig.generic.auto._
import pureconfig.loadConfig

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main extends App {
  implicit val system              = ActorSystem("tweet_publisher")
  implicit val mat                 = ActorMaterializer()
  implicit val tweetJsonValueCodec = JsonCodecMaker.make[Tweet](CodecMakerConfig())

  val client = TwitterStreamingClient()
  val producerSettings =
    ProducerSettings[PKey, PVal](system, new ByteArraySerializer, new ByteArraySerializer)

  loadConfig[AppConfig].map(
    appConfig =>
      TweetProducer(client, appConfig.tweets).publishTo(Producer.plainSink(producerSettings))
  )

  Await.ready(system.whenTerminated, Duration.Inf)
}
