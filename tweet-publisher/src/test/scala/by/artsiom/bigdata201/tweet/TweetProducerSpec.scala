package by.artsiom.bigdata201.tweet

import akka.actor.ActorSystem
import akka.actor.Status.Success
import akka.stream.Supervision.Decider
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.scaladsl.Sink
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import by.artsiom.bigdata201.tweet.config.TweetsConfig
import com.danielasfregola.twitter4s.StreamingClients
import com.danielasfregola.twitter4s.entities._
import com.danielasfregola.twitter4s.entities.enums.FilterLevel.FilterLevel
import com.danielasfregola.twitter4s.entities.enums.Language.Language
import com.danielasfregola.twitter4s.entities.streaming.CommonStreamingMessage
import com.danielasfregola.twitter4s.http.clients.streaming.TwitterStream
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}

import scala.concurrent.duration._
import scala.concurrent.Future

class TweetProducerSpec
    extends TestKit(ActorSystem("tweet_producer_test"))
    with ImplicitSender
    with FlatSpecLike
    with MockFactory
    with BeforeAndAfterAll {
  import TweetProducerSpec._

  implicit val mat = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy({
    case e: Throwable =>
      e.printStackTrace()
      Supervision.Stop
  }: Decider))
  implicit val tweetJsonValueCodec = JsonCodecMaker.make[Tweet](CodecMakerConfig())

  val configuration = TweetsConfig(Topic, Tracks, Location, BufSize)
  val tweet  = TweetGenerator.rendomTweet(HashTag)

  "TweetProducer" should "publish one kafka message with correct key" in {
    val client = createClientMock(tweet)

    val tweetProducer = new TweetProducer with HashTagExtractor {
      override protected val twitterClient: StreamingClients = client
      override protected val config: TweetsConfig = configuration
    }

    val probe = TestProbe()
    val actor = tweetProducer.publishTo(Sink.actorRef(probe.ref, "completed"))

    val message = probe.expectMsgPF(3 seconds) {
      case m: PMessage => m
      case _           => fail("wrong message type")
    }
    assert(new String(message.key()).contains(HashTag))

    actor ! Success
    probe.expectMsg(3 seconds, "completed")
  }

  override def afterAll: Unit =
    TestKit.shutdownActorSystem(system)

  private def createClientMock(tweet: Tweet): StreamingClients = {
    val client = mock[StreamingClients]

    (client.filterStatuses(
      _: Seq[Long],
      _: Seq[String],
      _: Seq[Double],
      _: Seq[Language],
      _: Boolean,
      _: FilterLevel
    )(_: PartialFunction[CommonStreamingMessage, Unit])) expects (*, *, *, *, *, *, *) onCall {
      (_: Seq[Long],
       _: Seq[String],
       _: Seq[Double],
       _: Seq[Language],
       _: Boolean,
       _: FilterLevel,
       fn: PartialFunction[CommonStreamingMessage, Unit]) =>
        fn(tweet)
        Future.successful(
          TwitterStream(ConsumerToken("", ""), AccessToken("", ""))(null, null, system)
        )
    }

    client
  }
}

object TweetProducerSpec {
  val Parallelism = 10
  val Topic       = "test"
  val Tracks      = List("big", "data")
  val Location    = List(0.02, 0.03, 0.04, 0.05)
  val BufSize     = 10
  val HashTag     = "first"
}
