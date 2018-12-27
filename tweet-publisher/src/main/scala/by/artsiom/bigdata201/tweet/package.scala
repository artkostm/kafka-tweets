package by.artsiom.bigdata201

import java.util.Date

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonReader, JsonValueCodec, JsonWriter}
import org.apache.kafka.clients.producer.ProducerRecord

package object tweet {
  type PKey     = Array[Byte]
  type PVal     = Array[Byte]
  type PMessage = ProducerRecord[PKey, PVal]

  implicit object DateJsonValueCodec extends JsonValueCodec[Date] {
    override def decodeValue(in: JsonReader, default: Date): Date = new Date(in.readLong())

    override def encodeValue(x: Date, out: JsonWriter): Unit = out.writeVal(x.getTime)

    override def nullValue: Date = null
  }
}
