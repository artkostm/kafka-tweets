package by.artsiom.bigdata201

package object job {

  final case class AppConfig(kafka: KafkaConfig, output: OutputConfig)
  final case class KafkaConfig(bootstrapServers: String,
                               topic: String,
                               startingOffsets: String = "earliest",
                               endingOffsets: String = "latest",
                               checkpointLocation: String)
  final case class OutputConfig(kafkaDataHdfsPath: String, hashtagCountsPath: String)
}
