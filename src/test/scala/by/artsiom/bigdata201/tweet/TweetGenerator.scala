package by.artsiom.bigdata201.tweet

import com.danielasfregola.randomdatagenerator.RandomDataGenerator
import com.danielasfregola.twitter4s.entities.{Entities, HashTag, Tweet}

object TweetGenerator extends RandomDataGenerator {

  def rendomTweet(hashTag: String): Tweet =
    random[Tweet].copy(entities = Some(Entities(hashtags = List(HashTag(hashTag, List())))))
}
