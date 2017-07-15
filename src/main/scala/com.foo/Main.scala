package com.foo

import org.apache.log4j.Logger.getRootLogger
import org.apache.log4j.Level.WARN
import org.apache.spark.{ SparkConf, SparkContext }

/**
 * Demonstrate race conditions in [[org.apache.spark.util.LongAccumulator]]s.
 *
 * Usage:
 *
 *   $ sbt package
 *   $ spark-submit accumulator-race.jar [repetitions=20] [numElems=10] [partitions=5] 2> /dev/null  # silence Spark's default stderr logging
 *
 * With default arguments, one or two of the 10 repetitions typically fail when running on a 4-core macbook pro. With
 * larger arguments, e.g. {20,100,100}, many failures are observed.
 *
 * Only the accumulators declared in the separate object [[Spark]] have been shown to produce incorrect results, for
 * some unknown reason.
 */
object Main {

  import Spark._

  def main(arguments: Array[String]): Unit = {

    var args = arguments
    def arg(default: Int): Int =
      if (args.length > 0) {
        val n = args(0).toInt
        args = args.drop(1)
        n
      } else
        default

    val repetitions = arg(20)
    val numElems = arg(10)
    val partitions = arg(5)
    var errors = 0

    println(
      s"Testing $repetitions repetitions of $numElems elements in $partitions partitions"
    )

    val localAccum = sc.longAccumulator("local")
    lazy val lazyLocalAccum = sc.longAccumulator("lazy-local")

    for {
      i ← 1 to repetitions
    } {
      otherAccum.reset
      lazyOtherAccum.reset
      localAccum.reset
      lazyLocalAccum.reset

      sc
        .parallelize(
          1 to numElems,
          numSlices = partitions
        )
        .mapPartitions {
          it ⇒
            otherAccum.add(1)
            lazyOtherAccum.add(1)
            localAccum.add(1)
            lazyLocalAccum.add(1)

            it.map(_.toString)
        }
        .collect()

      val values =
        Seq(
          otherAccum,
          lazyOtherAccum,
          localAccum,
          lazyLocalAccum
        )
        .map(_.value)

      if (!values.forall(_ == partitions)) {
        errors += 1
        println(
          Seq(
            s"Error: iteration $i:",
            s"other: ${otherAccum.value}",
            s"lazyOther: ${lazyOtherAccum.value}",
            s"local: ${localAccum.value}",
            s"lazyLocal: ${lazyLocalAccum.value}",
            ""
          )
          .mkString("\n\t")
        )
      }
    }

    println(
      errors match {
        case 0 ⇒ "No errors!"
        case _ ⇒ s"$errors errors"
      }
    )
  }
}

object Spark {
  getRootLogger.setLevel(WARN)
  val conf = new SparkConf
  conf.setMaster("local[4]")
  conf.setAppName("Accumulator Test")
  val sc = new SparkContext(conf)
  sc.setLogLevel("WARN")

  val otherAccum = sc.longAccumulator("other")
  lazy val lazyOtherAccum = sc.longAccumulator("lazy-other")
}
