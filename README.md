## `BlockInfoManager.releaseAllLocksForTask` `None.get`

[Main.scala](src/main/scala/Main.scala): repro of a stack trace discussed in [SPARK-16599](https://issues.apache.org/jira/browse/SPARK-16599).

In the presence of multiple `SparkContext`s, a broadcast block is not found, causing the app to crash.

To run:
```
sbt package
spark-submit block-exception.jar
```

[Full stack trace example here](https://gist.github.com/ryan-williams/59097a936c2158a8bdeba6c010fea0a3); relevant stack traces:

```
org.apache.spark.SparkException: Failed to get broadcast_0_piece0 of broadcast_0
	at org.apache.spark.broadcast.TorrentBroadcast$$anonfun$org$apache$spark$broadcast$TorrentBroadcast$$readBlocks$1.apply$mcVI$sp(TorrentBroadcast.scala:178)
	at org.apache.spark.broadcast.TorrentBroadcast$$anonfun$org$apache$spark$broadcast$TorrentBroadcast$$readBlocks$1.apply(TorrentBroadcast.scala:150)
	at org.apache.spark.broadcast.TorrentBroadcast$$anonfun$org$apache$spark$broadcast$TorrentBroadcast$$readBlocks$1.apply(TorrentBroadcast.scala:150)
	at scala.collection.immutable.List.foreach(List.scala:381)
	at org.apache.spark.broadcast.TorrentBroadcast.org$apache$spark$broadcast$TorrentBroadcast$$readBlocks(TorrentBroadcast.scala:150)
	at org.apache.spark.broadcast.TorrentBroadcast$$anonfun$readBroadcastBlock$1.apply(TorrentBroadcast.scala:218)
	at org.apache.spark.util.Utils$.tryOrIOException(Utils.scala:1269)
```

and later:

```
17/07/22 02:08:11 ERROR Executor: Exception in task 1.0 in stage 0.0 (TID 1)
java.util.NoSuchElementException: None.get
	at scala.None$.get(Option.scala:347)
	at scala.None$.get(Option.scala:345)
	at org.apache.spark.storage.BlockInfoManager.releaseAllLocksForTask(BlockInfoManager.scala:343)
	at org.apache.spark.storage.BlockManager.releaseAllLocksForTask(BlockManager.scala:670)
	at org.apache.spark.executor.Executor$TaskRunner.run(Executor.scala:289)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	at java.lang.Thread.run(Thread.java:745)
```
