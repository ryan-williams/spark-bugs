
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.1.0",
  // Having this on the classpath causes Spark to fail LargeBlockTest.
  "io.netty" % "netty-all" % "4.1.6.Final"
)
