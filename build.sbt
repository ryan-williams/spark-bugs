
scalaVersion := "2.11.8"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.0"

mainClass in (Compile, packageBin) := Some("com.foo.Main")

artifactName := ((_, _, _) ⇒ "block-exception.jar")
crossTarget in packageBin := baseDirectory.value
