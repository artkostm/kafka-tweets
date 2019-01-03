package by.artsiom.bigdata201.streaming

import by.artsiom.bigdata201.schema._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.spark.sql.types.StringType
import pureconfig.generic.auto._
import pureconfig.loadConfig

object Main extends App {
  loadConfig[AppConfig].fold(
    failure => sys.error(s"Please check your configuration: ${failure.toList.mkString(",")}"),
    runJob(_, SparkSession.builder().master("local[*]").getOrCreate())
  )

  def runJob(appConfig: AppConfig, spark: SparkSession): Unit =
    appConfig match {
      case AppConfig(source, sink, streaming) =>
        val kafkaStreamDF = spark.readStream
          .format("kafka")
          .option("kafka.bootstrap.servers", source.bootstrapServers)
          .option("subscribe", source.topic)
          .option("startingOffsets", source.startingOffsets)
          .load()

        import spark.implicits._

        kafkaStreamDF
          .select(
            from_json(ToUtfString($"value"), TweetSchema).as("tweet"),
            split(ToUtfString($"key"), ":").as("key")
          )
          .withColumn("tag", $"key" (1))
          .withColumn("date", to_timestamp(from_unixtime(ToSeconds($"tweet.created_at"), "yyyy-MM-dd HH:mm:ss")))
          .withWatermark("date", streaming.watermarkDelayThreshold)
          .groupBy(
            window($"date", streaming.windowDuration),
            $"tag"
          )
          .count()
          .select($"tag".alias("key"), $"count".cast(StringType).alias("value"))
          .writeStream
          .format("kafka")
          .option("kafka.bootstrap.servers", sink.bootstrapServers)
          .option("topic", sink.topic)
          .option("checkpointLocation", sink.checkpointLocation)
          .queryName("hashtag counts")
          .outputMode(OutputMode.Update())
          .trigger(Trigger.ProcessingTime(streaming.triggerInterval))
          .start()


        spark.streams.awaitAnyTermination()
    }
}
