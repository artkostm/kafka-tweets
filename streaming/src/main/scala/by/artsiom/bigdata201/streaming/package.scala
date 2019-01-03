package by.artsiom.bigdata201

package object streaming {
  final case class AppConfig(source: KafkaConfig, sink: KafkaConfig, streaming: StreamingConfig)
  final case class KafkaConfig(bootstrapServers: String,
                               topic: String,
                               startingOffsets: String = "latest",
                               checkpointLocation: String)
  final case class StreamingConfig(windowDuration: String, triggerInterval: String, watermarkDelayThreshold: String)
}
