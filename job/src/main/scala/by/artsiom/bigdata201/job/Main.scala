package by.artsiom.bigdata201.job

import java.nio.charset.StandardCharsets

import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions._
import pureconfig.generic.auto._
import pureconfig.loadConfig


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

    // converts byte array to utf-8 string
    val toUtfString = udf((payload: Array[Byte]) => new String(payload, StandardCharsets.UTF_8))

    // converts milliseconds to seconds
    val toSeconds = udf((ms: Long) => (ms / 1000))

    // writing hashtag counts to hdfs
    hdfsDF
      .select(
        from_json(toUtfString($"value"), tweetSchema).as("tweet"),
        split(toUtfString($"key"), ":").as("key")
      )
      .withColumn("tag", $"key"(1))
      .withColumn("date", from_unixtime(toSeconds($"tweet.created_at"), "YYYY-MM-dd"))
      .withColumn("hour", hour(from_unixtime(toSeconds($"tweet.created_at"), "yyyy-MM-dd HH:mm:ss")))
      .groupBy(
        "tag", "date", "hour"
      )
      .count()
      .write
      .mode(SaveMode.Overwrite)
      .partitionBy("date", "hour")
      .parquet(output.hashtagCountsPath)

    spark.stop()
  }
}
