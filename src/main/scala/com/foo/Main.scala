package com.foo

import java.nio.file.Files

import caseapp.core.ArgParser.instance
import caseapp.{ ExtraName ⇒ O, _ }
import org.apache.spark.{ SparkConf, SparkContext }

import scala.util.Random.nextInt
import scala.util.matching.Regex
import com.foo.EventTypeGroup._
import org.hammerlab.paths.Path
import org.hammerlab.stats.Stats

case class Opts(@O("n") n: Int = 1000,
                @O("c") cache: Boolean = false,
                @O("t") eventTypeGroups: List[EventTypeGroup] = List(Tasks, Other))

object Main extends CaseApp[Opts] {

  override def run(opts: Opts, args: RemainingArgs): Unit = {
    val logDir = Files.createTempDirectory("spark-logs").toString
    writeLog(opts, logDir)
    analyzeLogSize(opts, logDir)
  }

  def writeLog(opts: Opts, logDir: String): Unit = {
    val conf =
      new SparkConf()
        .set("spark.eventLog.enabled", "true")
        .set("spark.eventLog.dir", logDir)

    val sc = new SparkContext(conf)

    val rdd =
      sc
        .parallelize(1 to opts.n, opts.n)
        .flatMap {
          i ⇒
            (1 to 100)
              .map(
                _ ⇒ nextInt(i)
              )
        }

    if (opts.cache)
      rdd.cache

    println(s"${rdd.count} elements")

    sc.stop
  }

  def analyzeLogSize(opts: Opts, logDirStr: String): Unit = {
    val sc = new SparkContext

    val eventTypeGroupsBroadcast = sc.broadcast(opts.eventTypeGroups)

    val logDir = Path(logDirStr)

    val logFile =
      logDir.list.toVector match {
        case Vector(logFile) ⇒ logFile
        case files ⇒
          throw new IllegalStateException(
            s"Expected exactly one log file in directory $logDir; found ${files.length}:\n${files.mkString("\t", "\n\t", "")}"
          )
      }

    val stats: Array[(String, Stats[Int, Int])] =
      sc
        .textFile(logFile.toString)
        .flatMap {
          line ⇒
            val eventType =
              line
                .split("\"")(3)
                .drop("SparkListener".length)

            eventTypeGroupsBroadcast
              .value
              .zipWithIndex
              .filter(
                _._1.matches(eventType)
              )
              .map {
                case (EventTypeGroup(name, _), idx) ⇒
                  (idx, name) → line.length
              }
        }
        .groupByKey()
        .mapValues(Stats(_))
        .collect()
        .sorted
        .map { case ((_, group), stats) ⇒ group → stats }

    println(s"Analyzing log file $logFile (size ${logFile.size}):")
    println(
      stats
        .map {
          case (name, stats) ⇒
            s"$name:\n$stats\n"
        }
        .mkString("\n")
    )
  }
}
