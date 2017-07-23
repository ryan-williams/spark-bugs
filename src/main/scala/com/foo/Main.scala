package com.foo

import org.hammerlab.paths.Path

object Main {
  def main(args: Array[String]): Unit = {
    val path = Path("gs://hammerlab-spark/conf/full")
    println("loaded path")
    println(path.size)
  }
}
