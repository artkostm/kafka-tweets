package by.artsiom.bigdata201

import org.apache.kafka.clients.producer.ProducerRecord

package object tweet {
  type PKey     = Array[Byte]
  type PVal     = Array[Byte]
  type PMessage = ProducerRecord[PKey, PVal]
}
