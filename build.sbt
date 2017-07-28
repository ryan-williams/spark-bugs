
deps ++= Seq(
  case_app,
  magic_rdds % "1.5.0-SNAPSHOT",
  paths % "1.1.1-SNAPSHOT"
)

providedDeps += spark

assemblyJarName in assembly := "event-log-lengths.jar"
