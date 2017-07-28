
scalaVersion := "2.11.8"

libraryDependencies ++=
  Seq(
    "org.apache.spark" %% "spark-core" % "2.2.0" % "provided",
    "org.hammerlab" %% "paths" % "1.1.1-SNAPSHOT",
    "com.google.cloud" % "google-cloud-nio" % "0.20.3-alpha"
  )

mainClass in (Compile, packageBin) := Some("com.foo.Main")

artifactName := ((_, _, _) ⇒ "nio-test.jar")
crossTarget in packageBin := baseDirectory.value

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
