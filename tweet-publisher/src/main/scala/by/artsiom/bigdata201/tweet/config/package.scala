package by.artsiom.bigdata201.tweet

package object config {
  case class AppConfig(tweets: TweetsConfig)
  case class TweetsConfig(topic: String,
                          tracks: Seq[String],
                          locations: Seq[Double],
                          streamBufSize: Int)
}
