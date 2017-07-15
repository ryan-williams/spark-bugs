package com.foo

/**
 * Demonstration that adding to [[Long]]s is not thread-safe.
 */
object LongRace {
  def main(args: Array[String]): Unit = {
    var long = 0L

    val n =
      if (args.length > 0)
        args(0).toInt
      else
        1000

    def add(n: Int): Unit = {
      for {
        i ← 1 to n
      } {
        long += i
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

    println(long)
  }
}
