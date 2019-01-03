package by.artsiom.bigdata201.job

import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions._
import pureconfig.generic.auto._
import pureconfig.loadConfig

import by.artsiom.bigdata201.schema._

object Main extends App {

  loadConfig[AppConfig].fold(
    failure => sys.error(s"Please check your configuration: ${failure.toList.mkString(",")}"),
    runJob(_, SparkSession.builder().getOrCreate())
  )

  def runJob(appConfig: AppConfig, spark: SparkSession): Unit = {
    val Some((kafka, output)) = AppConfig.unapply(appConfig)

    // writing kafka topic to hdfs
    spark.read
      .format("kafka")
      .option("kafka.bootstrap.servers", kafka.bootstrapServers)
      .option("subscribe", kafka.topic)
      .option("startingOffsets", kafka.startingOffsets)
      .option("endingOffsets", kafka.endingOffsets)
      .load()
      .write
      .mode(SaveMode.Append)
      .option("checkpointLocation", kafka.checkpointLocation)
      .parquet(output.kafkaDataHdfsPath)

    val hdfsDF = spark.read
      .parquet(output.kafkaDataHdfsPath)

    import spark.implicits._

    // writing hashtag counts to hdfs
    hdfsDF
      .select(
        from_json(ToUtfString($"value"), TweetSchema).as("tweet"),
        split(ToUtfString($"key"), ":").as("key")
      )
      .withColumn("tag", $"key" (1))
      .withColumn("date", from_unixtime(ToSeconds($"tweet.created_at"), "YYYY-MM-dd"))
      .withColumn("hour",
                  hour(from_unixtime(ToSeconds($"tweet.created_at"), "yyyy-MM-dd HH:mm:ss")))
      .groupBy(
        "tag",
        "date",
        "hour"
      )
      .count()
      .write
      .mode(SaveMode.Overwrite)
      .partitionBy("date", "hour")
      .parquet(output.hashtagCountsPath)

    spark.stop()
  }
}
