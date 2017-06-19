package com.foo

import org.apache.spark.{ SparkConf, SparkContext }
import org.scalatest.FunSuite

/**
 * Test that demonstrates "large" (over 1MB?) result fetching failing in the presence of netty classpath-version
 * mismatch.
 */
class LargeBlockTest
  extends FunSuite {

  /**
   * [0,1032] pass, [1033,∞) fail.
   *
   * 1032 * [1000 bytes + C] is right around what we'd expect if a ≥1MB (1048576 bytes) block-size caused the failure.
   */
  for { n ← 1030 to 1035 } {
    check(n)
  }

  /**
   * Run a test case that collects `n` 1000-element [[Array]]s of [[Byte]]s – from one partition – to the driver.
   */
  def check(n: Int): Unit = {
    test(s"$n") {

      val sc = new SparkContext(conf)

      try {
        sc
          .parallelize(
            1 to n,
            numSlices = 1
          )
          .map(
            i ⇒
              Array.fill(1000)(i.toByte)
          )
          .collect
      } finally {
        sc.stop()
      }
    }
  }

  val conf =
    new SparkConf()
      .set("spark.master", "local[4]")
      .set("spark.app.name", "LargeBlockTest")
}
