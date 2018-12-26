package by.artsiom.bigdata201.tweet

import akka.stream.{ActorMaterializer, Graph, OverflowStrategy, SinkShape}
import akka.stream.scaladsl.{Flow, Source}
import by.artsiom.bigdata201.tweet.config.TweetsConfig
import com.danielasfregola.twitter4s.StreamingClients
import com.danielasfregola.twitter4s.entities.Tweet
import com.danielasfregola.twitter4s.entities.enums.Language
import com.github.plokhotnyuk.jsoniter_scala.core.{writeToArray, JsonValueCodec}

trait TweetProducer {
  protected val twitterClient: StreamingClients

  def publish(config: TweetsConfig, kafkaProducerSink: Graph[SinkShape[PMessage], _])(
    implicit mat: ActorMaterializer,
    tweetValueCodec: JsonValueCodec[Tweet]
  ) = {
    val streamRef = Source
      .actorRef[Tweet](config.streamBufSuze, OverflowStrategy.dropHead)
      .via(
        Flow[Tweet].flatMapConcat { tweet =>
          val hashtags = for {
            entities <- tweet.entities
          } yield entities.hashtags

          Source(hashtags.getOrElse(List.empty).toSet)
            .map(
              h =>
                new PMessage(config.topic,
                             (s"${tweet.user.map(_.name).getOrElse()}:${h.text}").getBytes,
                             writeToArray(tweet))
            )
        }
      )
      .to(kafkaProducerSink)
      .run()

    twitterClient.filterStatuses(
      tracks = Seq(config.tracks.mkString(",")),
      locations = config.locations,
      languages = Seq(Language.English, Language.Russian)
    ) {
      case t: Tweet => streamRef ! t
    }

    streamRef
  }
}

object TweetProducer {

  def apply(client: StreamingClients): TweetProducer = new TweetProducer {
    override protected val twitterClient: StreamingClients = client
  }
}
