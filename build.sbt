
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.1.1",
  // Having this on the classpath causes Spark to fail LargeBlockTest.
  "io.netty" % "netty-all" % "4.1.6.Final",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)
