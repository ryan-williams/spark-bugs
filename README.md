## Missing `FileCommitProtocol$TaskCommitMessage` kryo registration 

To run/repro:

```bash
sbt package

# Passes in Spark 2.1.1, fails in Spark 2.2.0
spark-submit save-hadoop-file.jar
```

As of Spark 2.2.0, [the `saveAsNewAPIHadoopFile` job](src/main/scala/com/foo/Main.scala#L26) fails with:

```
java.lang.IllegalArgumentException: Class is not registered: org.apache.spark.internal.io.FileCommitProtocol$TaskCommitMessage
Note: To register this class use: kryo.register(org.apache.spark.internal.io.FileCommitProtocol$TaskCommitMessage.class);
	at com.esotericsoftware.kryo.Kryo.getRegistration(Kryo.java:488)
	at com.esotericsoftware.kryo.util.DefaultClassResolver.writeClass(DefaultClassResolver.java:97)
	at com.esotericsoftware.kryo.Kryo.writeClass(Kryo.java:517)
	at com.esotericsoftware.kryo.Kryo.writeClassAndObject(Kryo.java:622)
	at org.apache.spark.serializer.KryoSerializerInstance.serialize(KryoSerializer.scala:315)
	at org.apache.spark.executor.Executor$TaskRunner.run(Executor.scala:383)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	at java.lang.Thread.run(Thread.java:745)
```
