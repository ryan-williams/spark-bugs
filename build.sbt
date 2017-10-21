scalaVersion := "2.11.8"
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.0"
artifactName := ((_, _, _) ⇒ "closure-serialization-bug.jar")
crossTarget in packageBin := baseDirectory.value
