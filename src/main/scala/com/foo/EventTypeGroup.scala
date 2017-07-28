package com.foo

import caseapp.core.ArgParser.instance

import scala.util.matching.Regex

case class EventTypeGroup(name: String,
                          regexs: Seq[Regex]) {
  def matches(eventType: String): Boolean =
    regexs
      .exists(
        _
          .findFirstIn(eventType)
          .isDefined
      )
}

object EventTypeGroup {
  val Tasks = EventTypeGroup("Tasks", Seq("^Task".r))
  val Other = EventTypeGroup("Other", Seq("^[^T]".r))

  val regex = "([^:]+):(.*)".r

  implicit val parser =
    instance("event type group") {
      case regex(name, regexs) ⇒
        Right(
          EventTypeGroup(
            name,
            regexs
              .split(",")
              .map(_.r)
          )
        )
      case arg ⇒
        Left(
          s"Invalid event-type group: $arg"
        )
    }
}
