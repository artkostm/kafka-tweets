package by.artsiom.bigdata201.tweet

import akka.actor.{ActorRef, ActorSystem}
import akka.actor.Status.Failure
import akka.stream.{ActorMaterializer, Graph, OverflowStrategy, SinkShape}
import akka.stream.scaladsl.{Flow, Source}
import by.artsiom.bigdata201.tweet.config.TweetsConfig
import com.danielasfregola.twitter4s.StreamingClients
import com.danielasfregola.twitter4s.entities.Tweet
import com.danielasfregola.twitter4s.entities.enums.{FilterLevel, Language}
import com.danielasfregola.twitter4s.entities.streaming.CommonStreamingMessage
import com.danielasfregola.twitter4s.entities.streaming.common.{
  DisconnectMessage,
  LimitNotice,
  LimitTrack,
  WarningMessage
}
import com.github.plokhotnyuk.jsoniter_scala.core.{writeToArray, JsonValueCodec}

trait TweetProducer {
  protected val twitterClient: StreamingClients
  protected val config: TweetsConfig

  def publishTo(kafkaProducerSink: Graph[SinkShape[PMessage], _])(
    implicit mat: ActorMaterializer,
    tweetValueCodec: JsonValueCodec[Tweet]
  ) = {
    val streamRef = Source
      .actorRef[Tweet](config.streamBufSize, OverflowStrategy.dropHead)
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
      tracks = config.tracks,
      //locations = config.locations,
      languages = Seq(Language.English, Language.Russian),
      stall_warnings = true,
      filter_level = FilterLevel.Medium
    )(TweetProducer.processMessages(mat.system, streamRef))

    streamRef
  }
}

object TweetProducer {

  def apply(client: StreamingClients, tc: TweetsConfig): TweetProducer = new TweetProducer {
    override protected val twitterClient: StreamingClients = client
    override protected val config: TweetsConfig            = tc
  }

  protected def processMessages(
    system: ActorSystem,
    streamRef: ActorRef
  ): PartialFunction[CommonStreamingMessage, Unit] = {
    case t: Tweet =>
      streamRef ! t
    case DisconnectMessage(info) =>
      system.log.error(s"""
                          |Stream ${info.stream_name} was disconnected due to reason: ${info.reason}
                          |Code: ${info.code}
                          |""".stripMargin)
      streamRef ! Failure(new RuntimeException("Twitter stream disconnected!"))
    case LimitNotice(LimitTrack(track)) =>
      system.log.warning(s"""
                            |Stream limit has been reached.
                            |A total count of the number of undelivered Tweets since the connection was opened is $track
           """.stripMargin)
    case WarningMessage(info) =>
      system.log.warning(s"""
                            |Got warning with code ${info.code}. Message: ${info.message}
                            |Percent full: ${info.percent_full}, userId=${info.user_id}.
           """.stripMargin)
  }
}
