package by.artsiom.bigdata201.tweet

import com.danielasfregola.twitter4s.entities.{Entities, ExtendedTweet, HashTag, Tweet}

trait HashTagExtractor {

  def hashTags(t: Tweet): Seq[String] = {
    val getTags: Option[Entities] => Seq[HashTag] = _ match {
      case Some(Entities(hashtags, _, _, _, _, _, _)) => hashtags
      case _                                          => Seq.empty
    }

    val tags            = getTags(t.entities)
    val extTags         = getTags(t.extended_entities)
    val extTweet        = t.extended_tweet.getOrElse(ExtendedTweet(null))
    val extTweetTags    = getTags(extTweet.entities)
    val extTweetExtTags = getTags(extTweet.extended_entities)

    (tags ++ extTags ++ extTweetTags ++ extTweetExtTags).map(_.text)
  }
}
