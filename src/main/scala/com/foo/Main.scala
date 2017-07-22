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

    // Instantiate first/main SparkContext
    val conf = new SparkConf()
    conf.setMaster("local[4]")
    conf.setAppName("Main")
    implicit val sc = new SparkContext(conf)

    // Make a Broadcast
    val bs = sc.broadcast(Set(1, 2, 3))

    // Set some other ambient system properties that help a second SparkContext be able to be created
    setProperty("spark.master", "local[4]")
    setProperty("spark.app.name", "Test2")
    setProperty("spark.driver.allowMultipleContexts", "true")

    // "Accidentally" create second SparkContext
    println(Foo.foo)

    val path = "src/main/resources/test"

    sc
      .textFile(path)

      // Reference Broadcast in a task
      .filter(line ⇒ bs.value(line.length))

      // Run job ⟶ BlockManager crashes
      .count
  }
}
