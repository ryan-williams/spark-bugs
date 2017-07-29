package com.foo

import java.nio.file.Files

import com.esotericsoftware.kryo.Kryo
import org.apache.hadoop.fs.Path
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat
import org.apache.spark.serializer.{ KryoRegistrator, KryoSerializer }
import org.apache.spark.{ SparkConf, SparkContext }

object Main {
  def main(args: Array[String]): Unit = {
    val conf =
      new SparkConf()
        .set("spark.serializer", classOf[KryoSerializer].getCanonicalName)
        .set("spark.kryo.registrationRequired", "true")
        .set("spark.kryo.registrator", classOf[Registrar].getCanonicalName)

    val sc = new SparkContext(conf)

    val dir = Files.createTempDirectory("test")
    val path = dir.resolve("foo")
    sc
      .parallelize(1 to 100)
      .map(x ⇒ x.toString → x.toString)
      .saveAsNewAPIHadoopFile[TextOutputFormat[String, String]](path.toString)

    val hpath = new Path(dir.toUri)
    hpath.getFileSystem(sc.hadoopConfiguration).delete(hpath, true)

    sc.stop()
  }
}

class Registrar extends KryoRegistrator {
  override def registerClasses(kryo: Kryo): Unit = {
    kryo.register(classOf[Range])
  }
}
