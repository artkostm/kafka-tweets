package by.artsiom.bigdata201.tweet

import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Source}
import by.artsiom.bigdata201.tweet.config.TweetsConfig
import com.danielasfregola.twitter4s.{StreamingClients, TwitterStreamingClient}
import com.danielasfregola.twitter4s.entities.{ExtendedTweet, Tweet}
import com.danielasfregola.twitter4s.entities.enums.Language
import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, writeToArray}

trait TweetProducer {
  protected val twitterClient: StreamingClients

  def publish(config: TweetsConfig)(
    implicit producerSettings: ProducerSettings[PKey, PVal],
    mat: ActorMaterializer,
    tweetValueCodec: JsonValueCodec[Tweet]
  ) = {
    val streamRef = Source
      .actorRef[Tweet](bufferSize = 10, overflowStrategy = OverflowStrategy.backpressure)
      .via(
        Flow[Tweet].map(
          t =>
            new PMessage(config.topic,
                         t.entities.map(_.hashtags.mkString(",")).getOrElse("").getBytes,
                         writeToArray(t))
        )
      )
      .to(Producer.plainSink(producerSettings))
      .run()

    twitterClient.filterStatuses(
      tracks = Seq(config.tracks.mkString(",")),
      locations = config.locations,
      languages = Seq(Language.English, Language.Russian)
    ) {
      case t: Tweet => streamRef ! t
    }
  }
}

object TweetProducer {

  def apply(client: StreamingClients): TweetProducer = new TweetProducer {
    override protected val twitterClient: StreamingClients = client
  }
}
