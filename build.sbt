
scalaVersion := "2.11.8"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.0" % "provided"

artifactName := ((_, _, _) ⇒ "save-hadoop-file.jar")
crossTarget in packageBin := baseDirectory.value
