# spark-bugs
Dumping ground for repros of issues with Apache Spark

## 1MB block + Netty 4.1.6.Final error

Run [the test](src/test/scala/com/foo/LargeBlockTest.scala):

```
git clone https://github.com/ryan-williams/spark-bugs.git
cd spark-bugs
sbt test
```

You'll see several errors like this:

```
17/06/18 21:16:43 ERROR TransportClient: Failed to send RPC 4953895693497783001 to /172.27.5.37:55620: java.lang.AbstractMethodError: org.apache.spark.network.protocol.MessageWithHeader.touch(Ljava/lang/Object;)Lio/netty/util/ReferenceCounted;
java.lang.AbstractMethodError: org.apache.spark.network.protocol.MessageWithHeader.touch(Ljava/lang/Object;)Lio/netty/util/ReferenceCounted;
	at io.netty.util.ReferenceCountUtil.touch(ReferenceCountUtil.java:73)
	at io.netty.channel.DefaultChannelPipeline.touch(DefaultChannelPipeline.java:107)
	at io.netty.channel.AbstractChannelHandlerContext.write(AbstractChannelHandlerContext.java:821)
	at io.netty.channel.AbstractChannelHandlerContext.write(AbstractChannelHandlerContext.java:734)
	at io.netty.handler.codec.MessageToMessageEncoder.write(MessageToMessageEncoder.java:111)
	at io.netty.channel.AbstractChannelHandlerContext.invokeWrite0(AbstractChannelHandlerContext.java:749)
	at io.netty.channel.AbstractChannelHandlerContext.invokeWrite(AbstractChannelHandlerContext.java:741)
	at io.netty.channel.AbstractChannelHandlerContext.write(AbstractChannelHandlerContext.java:827)
	at io.netty.channel.AbstractChannelHandlerContext.write(AbstractChannelHandlerContext.java:734)
	at io.netty.handler.timeout.IdleStateHandler.write(IdleStateHandler.java:284)
	at io.netty.channel.AbstractChannelHandlerContext.invokeWrite0(AbstractChannelHandlerContext.java:749)
	at io.netty.channel.AbstractChannelHandlerContext.invokeWrite(AbstractChannelHandlerContext.java:741)
	at io.netty.channel.AbstractChannelHandlerContext.access$1900(AbstractChannelHandlerContext.java:39)
	at io.netty.channel.AbstractChannelHandlerContext$AbstractWriteTask.write(AbstractChannelHandlerContext.java:1100)
	at io.netty.channel.AbstractChannelHandlerContext$WriteAndFlushTask.write(AbstractChannelHandlerContext.java:1147)
	at io.netty.channel.AbstractChannelHandlerContext$AbstractWriteTask.run(AbstractChannelHandlerContext.java:1089)
	at io.netty.util.concurrent.AbstractEventExecutor.safeExecute(AbstractEventExecutor.java:163)
	at io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:418)
	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:454)
	at io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:873)
	at io.netty.util.concurrent.DefaultThreadFactory$DefaultRunnableDecorator.run(DefaultThreadFactory.java:144)
	at java.lang.Thread.run(Thread.java:745)
```

and ultimately test failures:

```
[info] - 1033 *** FAILED ***
[info]   org.apache.spark.SparkException: Job aborted due to stage failure: Task 0 in stage 0.0 failed 1 times, most recent failure: Lost task 0.0 in stage 0.0 (TID 0, localhost, executor driver): TaskResultLost (result lost from block manager)
[info] Driver stacktrace:
[info]   at org.apache.spark.scheduler.DAGScheduler.org$apache$spark$scheduler$DAGScheduler$$failJobAndIndependentStages(DAGScheduler.scala:1435)
[info]   at org.apache.spark.scheduler.DAGScheduler$$anonfun$abortStage$1.apply(DAGScheduler.scala:1423)
[info]   at org.apache.spark.scheduler.DAGScheduler$$anonfun$abortStage$1.apply(DAGScheduler.scala:1422)
[info]   at scala.collection.mutable.ResizableArray$class.foreach(ResizableArray.scala:59)
[info]   at scala.collection.mutable.ArrayBuffer.foreach(ArrayBuffer.scala:47)
[info]   at org.apache.spark.scheduler.DAGScheduler.abortStage(DAGScheduler.scala:1422)
[info]   at org.apache.spark.scheduler.DAGScheduler$$anonfun$handleTaskSetFailed$1.apply(DAGScheduler.scala:802)
[info]   at org.apache.spark.scheduler.DAGScheduler$$anonfun$handleTaskSetFailed$1.apply(DAGScheduler.scala:802)
[info]   at scala.Option.foreach(Option.scala:236)
[info]   at org.apache.spark.scheduler.DAGScheduler.handleTaskSetFailed(DAGScheduler.scala:802)
```

The test seemingly fails when the size of a task-result exceeds 1MB, and Netty `4.1.6.Final` is on the classpath (ahead of Spark 2.1.0's `4.0.42.Final`).
