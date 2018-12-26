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

object Main extends App {
  implicit val system              = ActorSystem("tweet_publisher")
  implicit val mat                 = ActorMaterializer()
  implicit val tweetJsonValueCodec = JsonCodecMaker.make[Tweet](CodecMakerConfig())
  implicit val producerSettings =
    ProducerSettings[PKey, PVal](system, new ByteArraySerializer, new ByteArraySerializer)

  val config = pureconfig.loadConfig[AppConfig]
  val client = TwitterStreamingClient()


  config.map(appConfig => TweetProducer(client).publish(appConfig.tweets, Producer.plainSink(producerSettings)))

  val ip = "128.42.5.4"

  val mask = "255.255.248.0"

  val ip32bits = java.lang.Long.parseLong(
    ip.split("\\.")
      .map(s => ("00000000" + (s.toInt.toBinaryString)).substring(s.toInt.toBinaryString.length))
      .mkString,
    2
  )

  val mask32bits = java.lang.Long.parseLong(
    mask
      .split("\\.")
      .map(s => ("00000000" + (s.toInt.toBinaryString)).substring(s.toInt.toBinaryString.length))
      .mkString,
    2
  )

  println(
    (ip32bits & mask32bits).toBinaryString.toCharArray
      .grouped(8)
      .map(_.mkString)
      .map(Integer.parseInt(_, 2))
      .mkString(".")
  )

}
