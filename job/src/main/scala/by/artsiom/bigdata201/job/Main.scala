package by.artsiom.bigdata201.job
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types.BinaryType

object Main extends App {

  val spark = SparkSession
    .builder()
    .master("local[*]")
    .getOrCreate()

  val kafkaStreamDF = spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "192.168.99.100:9092")
    .option("subscribe", "Tweets")
    .option("startingOffsets", "earliest")
    .load()

  import spark.implicits._

  kafkaStreamDF
    .coalesce(2)
    .select($"value".cast(BinaryType).as("tweet"), $"key".cast(BinaryType).as("key"))
    .map { row =>
      val tweet = new String(row.getAs[Array[Byte]]("tweet"))
      val key   = new String(row.getAs[Array[Byte]]("key"))
      (key.split(":")(1), tweet)
    }
    .withColumnRenamed("_1", "tag")
    .writeStream
    .partitionBy("tag")
    .format("parquet")
    .queryName("parquet_files_to_hdfs")
    .outputMode(OutputMode.Append())
    .option("path", "testDir")
    .option("checkpointLocation", "/tmp/st/")
    .start()

  spark.streams.awaitAnyTermination(100000)
}
