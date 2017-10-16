package com.foo

import org.apache.spark.{ SparkConf, SparkContext, SparkException }
import Filters._

/**
 * Filter-functions exercising different closure-serialization paths (multiple/implicit parameter lists,
 * partial-application).
 */
object Filters {
  def filter1(i: Int): Boolean                      = i % 3 != 0

  /** These take an extra argument that is a field of [[App]], namely [[App.s]] */
  def filter2(i: Int, s: String): Boolean           = i % 3 != 0  // tests partial-application
  def filter3(i: Int)(implicit s: String): Boolean  = i % 3 != 0  // test passing as an implicit parameter
  def filter5(i: Int)(s: String): Boolean           = i % 3 != 0  // test passing in a second argument list

  /** These take an extra argument that is locally declared in [[App.run]] */
  def filter6(i: Int, b: Boolean): Boolean          = i % 3 != 0  // tests partial-application
  def filter7(i: Int)(implicit b: Boolean): Boolean = i % 3 != 0  // test passing as an implicit parameter
  def filter9(i: Int)(b: Boolean): Boolean          = i % 3 != 0  // test passing in a second argument list
}

/** Non-nullary constructor, not directly [[Serializable]]: causes problems */
class Super1(n: Int)

/** Non-nullary constructor, mixes-in [[Serializable]]: works fine */
class Super2(n: Int) extends Serializable

/** Nullary constructor, not directly [[Serializable]]: works fine */
class Super3

trait App
  extends Serializable {

  /** Used as extra parameter to [[filter2]], [[filter3]], and [[filter5]] */
  implicit val s = "abc"

  /**
   * Run one of 9 simple filter+collect jobs on an RDD
   * @param n which filter to run, ∈ [1,9]
   * @return error msg, if applicable
   */
  def run(n: Int): Option[String] = {

    /** Make a simple [[org.apache.spark.rdd.RDD]] */
    val conf = new SparkConf().set("spark.master", "local[4]").set("spark.app.name", "serde-test")
    val sc = new SparkContext(conf)
    val rdd = sc.parallelize(1 to 10)

    /** Used as extra parameter to [[filter6]], [[filter7]], and [[filter9]] */
    implicit val b: Boolean = true

    try {
      n match {

        /** No extra arguments; always succeeds */
        case 1 ⇒ rdd.filter(filter1).collect

        /** Extra parameter [[s]] is a field of [[App]]; deserialization fails in [[App1]] */
        case 2 ⇒ rdd.filter(filter2(_, s)).collect
        case 3 ⇒ rdd.filter(filter3).collect
        case 4 ⇒ rdd.filter(filter3(_)(s)).collect
        case 5 ⇒ rdd.filter(filter5(_)(s)).collect

        /** Extra parameter [[b]] is scoped to [[App.run]]; always succeeds **/
        case 6 ⇒ rdd.filter(filter6(_, b)).collect
        case 7 ⇒ rdd.filter(filter7).collect
        case 8 ⇒ rdd.filter(filter7(_)(b)).collect
        case 9 ⇒ rdd.filter(filter9(_)(b)).collect
      }
      None
    } catch {
      case e: SparkException ⇒ Some(e.getCause.getMessage)
    } finally {
      sc.stop()
    }
  }
}

// Extends non-Serializable super-class with non-nullary constructor: fails when a closure references a field of App.
class App1 extends Super1(4) with App

// Same as above; directly mixing-in Serializable doesn't help
class App2 extends Super1(4) with App with Serializable

// Extends Serializable super-class with non-nullary constructor: everything is fine.
class App3 extends Super2(4) with App

// Extends non-Serializable super-class with nullary constructor: everything is fine.
class App4 extends Super3    with App

/**
 * Run each of the four [[App]]s above on each of 9 [[Filters filter-functions]], and print results.
 */
object Main {
  def main(args: Array[String]): Unit =
    Seq(
      new App1,
      new App2,
      new App3,
      new App4
    )
    .map {
      app ⇒
        app.getClass.getSimpleName →
          (1 to 9 map { i ⇒ i → app.run(i) })
    }
    .foreach {
      case (app, results) ⇒
        results.foreach {
          case (i, Some(msg)) ⇒
            println(s"$app, $i: failure: $msg")
          case (i, _) ⇒
            println(s"$app, $i: success")
        }
        println("")
    }
}
