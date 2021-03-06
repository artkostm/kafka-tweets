package by.artsiom.bigdata201

import java.nio.charset.StandardCharsets

import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.types._

package object schema {
  // converts byte array to utf-8 string
  val ToUtfString = udf((payload: Array[Byte]) => new String(payload, StandardCharsets.UTF_8))

  // converts milliseconds to seconds
  val ToSeconds = udf((ms: Long) => (ms / 1000))

  val TweetSchema = new StructType(
    Array(
      StructField("contributors",
                  ArrayType(
                    new StructType(
                      Array(
                        StructField("id", LongType),
                        StructField("id_str", StringType),
                        StructField("screen_name", StringType)
                      )
                    )
                  )),
      StructField("coordinates", new StructType()),
      StructField("created_at", LongType),
      StructField("current_user_retweet", new StructType()),
      StructField("entities", new StructType()),
      StructField("extended_entities", new StructType()),
      StructField("extended_tweet", new StructType()),
      StructField("favorite_count", IntegerType),
      StructField("favorited", BooleanType),
      StructField("filter_level", StringType),
      StructField("geo", new StructType()),
      StructField("id", LongType),
      StructField("id_str", StringType),
      StructField("in_reply_to_screen_name", StringType),
      StructField("in_reply_to_status_id", LongType),
      StructField("in_reply_to_status_id_str", StringType),
      StructField("in_reply_to_user_id", LongType),
      StructField("in_reply_to_user_id_str", StringType),
      StructField("is_quote_status", BooleanType),
      StructField("lang", StringType),
      StructField("place", new StructType()),
      StructField("possibly_sensitive", BooleanType),
      StructField("quoted_status_id", LongType),
      StructField("quoted_status_id_str", StringType),
      StructField("quoted_status", new StructType()),
      StructField("scopes", MapType(StringType, BooleanType)),
      StructField("retweet_count", LongType),
      StructField("retweeted", BooleanType),
      StructField("retweeted_status", new StructType()),
      StructField("source", StringType),
      StructField("text", StringType),
      StructField("truncated", BooleanType),
      StructField("display_text_range", ArrayType(IntegerType)),
      StructField("user", new StructType()),
      StructField("withheld_copyright", BooleanType),
      StructField("withheld_in_countries", ArrayType(StringType)),
      StructField("withheld_scope", StringType),
      StructField("metadata", new StructType())
    )
  )
}
