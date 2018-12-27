package by.artsiom.bigdata201.tweet

import java.util.Date

import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}
import com.github.plokhotnyuk.jsoniter_scala.core.{readFromArray, writeToArray}
import org.scalatest.FlatSpec

class DateJsonValueCodecSpec extends FlatSpec {
  case class TestCls(date: Date)

  implicit val testClsCodec = JsonCodecMaker.make[TestCls](CodecMakerConfig())

  "Codec" should "convert date to valid json Numeric (timestamp)" in {
    val date       = new Date()
    val actualJson = new String(writeToArray(TestCls(date)))
    assert(s"""{"date":${date.getTime}}""" == actualJson)
  }

  "Codec" should "convert json to correct date" in {
    val date                   = new Date()
    val json                   = s"""{"date":${date.getTime}}"""
    val actualTestCls: TestCls = readFromArray(json.getBytes)(testClsCodec)
    assert(date.getTime == actualTestCls.date.getTime)
  }
}
