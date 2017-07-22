package com.foo

import java.lang.System.setProperty

import org.apache.spark.{ SparkConf, SparkContext }

object Foo {
  def foo = 4

  private val sparkConf = new SparkConf()
  implicit val sc: SparkContext = new SparkContext(sparkConf)
}

object Main {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setMaster("local[4]")
    conf.setAppName("Main")

    implicit val sc = new SparkContext(conf)

    val bs = sc.broadcast(Set(1, 2, 3))

    setProperty("spark.master", "local[4]")
    setProperty("spark.app.name", "Test2")
    setProperty("spark.driver.allowMultipleContexts", "true")
    println(Foo.foo)

    val path = "src/main/resources/test"

    sc
      .textFile(path)
      .filter(line ⇒ bs.value(line.length))
      .count
  }
}
