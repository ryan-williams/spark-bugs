## [SPARK-22288](https://issues.apache.org/jira/browse/SPARK-22288): `no valid constructor` serialization error
 
Tricky interaction between:

- serialization
- inheritance
- closure-cleaning, and
- multiple parameter-lists / partial-function-application / implicit parameters

### Repro

```shell
# sbt run
…
App1, 1: success
App1, 2: failure: com.foo.App1; no valid constructor
App1, 3: failure: com.foo.App1; no valid constructor
App1, 4: failure: com.foo.App1; no valid constructor
App1, 5: failure: com.foo.App1; no valid constructor
App1, 6: success
App1, 7: success
App1, 8: success
App1, 9: success

App2, 1: success
App2, 2: failure: com.foo.App2; no valid constructor
App2, 3: failure: com.foo.App2; no valid constructor
App2, 4: failure: com.foo.App2; no valid constructor
App2, 5: failure: com.foo.App2; no valid constructor
App2, 6: success
App2, 7: success
App2, 8: success
App2, 9: success

App3, 1: success
App3, 2: success
App3, 3: success
App3, 4: success
App3, 5: success
App3, 6: success
App3, 7: success
App3, 8: success
App3, 9: success

App4, 1: success
App4, 2: success
App4, 3: success
App4, 4: success
App4, 5: success
App4, 6: success
App4, 7: success
App4, 8: success
App4, 9: success
``` 

See [Main.scala].

### Discussion

When a closure passed to an RDD `map` or `filter` references a field of a class that extends a non-`Serializable` `class` with a non-nullary constructor, deserialization of the class fails with a cryptic message / trace:

```
java.io.InvalidClassException: com.foo.App1; no valid constructor
	at java.io.ObjectStreamClass$ExceptionInfo.newInvalidClassException(ObjectStreamClass.java:150)
	at java.io.ObjectStreamClass.checkDeserialize(ObjectStreamClass.java:790)
	at java.io.ObjectInputStream.readOrdinaryObject(ObjectInputStream.java:1782)
	at java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1353)
	at java.io.ObjectInputStream.defaultReadFields(ObjectInputStream.java:2018)
	at java.io.ObjectInputStream.readSerialData(ObjectInputStream.java:1942)
	at java.io.ObjectInputStream.readOrdinaryObject(ObjectInputStream.java:1808)
	at java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1353)
	at java.io.ObjectInputStream.defaultReadFields(ObjectInputStream.java:2018)
	at java.io.ObjectInputStream.readSerialData(ObjectInputStream.java:1942)
	at java.io.ObjectInputStream.readOrdinaryObject(ObjectInputStream.java:1808)
	at java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1353)
	at java.io.ObjectInputStream.defaultReadFields(ObjectInputStream.java:2018)
	at java.io.ObjectInputStream.readSerialData(ObjectInputStream.java:1942)
	at java.io.ObjectInputStream.readOrdinaryObject(ObjectInputStream.java:1808)
	at java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1353)
	at java.io.ObjectInputStream.defaultReadFields(ObjectInputStream.java:2018)
	at java.io.ObjectInputStream.readSerialData(ObjectInputStream.java:1942)
	at java.io.ObjectInputStream.readOrdinaryObject(ObjectInputStream.java:1808)
	at java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1353)
	at java.io.ObjectInputStream.readObject(ObjectInputStream.java:373)
	at org.apache.spark.serializer.JavaDeserializationStream.readObject(JavaSerializer.scala:75)
	at org.apache.spark.serializer.JavaSerializerInstance.deserialize(JavaSerializer.scala:114)
	at org.apache.spark.scheduler.ResultTask.runTask(ResultTask.scala:80)
	at org.apache.spark.scheduler.Task.run(Task.scala:108)
	at org.apache.spark.executor.Executor$TaskRunner.run(Executor.scala:335)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	at java.lang.Thread.run(Thread.java:745)
```

The driver stack-trace will point to the line that triggered the Spark job that exercised the RDD `map` or `filter`, which can be extra confusing.

The exception is confusing for several reasons:
- the issue occurs even if the class itself `extends Serializable`, as long as its superclass does not directly extend `Serializable` (cf. [`App2`] vs. [`App3`])
- it only occurs if a closure references a *field* of the class, not if it references method-local variables (cf. [filters 2-5] vs. [6-9][filters 6-9]
- the reference can be invisible (e.g. to an `implicit` parameter) or inconspicuous / inline (as in a partial-application)


[`App2`]: https://github.com/ryan-williams/spark-bugs/blob/serde/src/main/scala/com/foo/Main.scala#L84-L85
[`App3`]: https://github.com/ryan-williams/spark-bugs/blob/serde/src/main/scala/com/foo/Main.scala#L87-L88

[filters 2-5]: https://github.com/ryan-williams/spark-bugs/blob/serde/src/main/scala/com/foo/Main.scala#L60-L64
[filters 6-9]: https://github.com/ryan-williams/spark-bugs/blob/serde/src/main/scala/com/foo/Main.scala#L66-L70

[Main.scala]: https://github.com/ryan-williams/spark-bugs/blob/serde/src/main/scala/com/foo/Main.scala
