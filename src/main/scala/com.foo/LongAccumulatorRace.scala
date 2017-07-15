package com.foo

import org.apache.spark.util.LongAccumulator

/**
 * Demonstration that adding to [[LongAccumulator]]s is not thread-safe.
 */
object LongAccumulatorRace {
  def main(args: Array[String]): Unit = {
    val long = new LongAccumulator

    val n =
      if (args.length > 0)
        args(0).toInt
      else
        1000

    def add(n: Int): Unit = {
      for {
        i ← 1 to n
      } {
        long.add(i)
      }
    }

    val thread =
      new Thread {
        override def run(): Unit = {
          add(n)
        }
      }

    thread.start()

    add(n)

    thread.join()

    println(long.value)
  }
}
