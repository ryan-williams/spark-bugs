
## Static [LongAccumulator][]/[DoubleAccumulator][] race condition

See discussion on [SPARK-21425](https://issues.apache.org/jira/browse/SPARK-21425).

### Running
```bash
git clone https://github.com/ryan-williams/spark-bugs.git
cd spark-bugs
git checkout accum
sbt package
spark-submit accumulator-race.jar [repetitions=20] [numElems=10] [partitions=5] 2> /dev/null  # silence Spark's default stderr logging
```

You'll likely see at least one of the default 20 repetitions fail:

```
Testing 20 repetitions of 10 elements in 5 partitions
Error: iteration 1:
	other: 3
	lazyOther: 3
	local: 5
	lazyLocal: 5

Error: iteration 4:
	other: 4
	lazyOther: 4
	local: 5
	lazyLocal: 5

Error: iteration 12:
	other: 4
	lazyOther: 5
	local: 5
	lazyLocal: 5

3 errors
```

If not, try running again! Increasing the numbers of elements and partitions also "helps" 😂.

### Discussion

tl;dr: don't make [LongAccumulator][]s/[DoubleAccumulator][]s static! In normal use, they won't be, but in some test-cases it's easy to want to make them static.

There are 4 [`LongAccumulator`][LongAccumulator]s declared in [`Main`]:

- 2 in `Main` itself:
	- 1 `lazy` ("`lazyLocal`")
	- 1 not ("`local`")
- 2 in the adjacent `Spark` object:
	- 1 `lazy` ("`lazyOther`")
	- 1 not ("`other`")

In tests, I've only observed `other` and `lazyOther` (the two [LongAccumulator]s declared in the `Spark` object) to have incorrect counts.

However, reasoning about the bug implies that it should generally apply to all `LongAccumulator`s that are written to in tasks that run on executors with more than one "core" (simultaneous task running).

[LongAccumulator]: https://github.com/apache/spark/blob/v2.2.0/core/src/main/scala/org/apache/spark/util/AccumulatorV2.scala#L291
[DoubleAccumulator]: https://github.com/apache/spark/blob/v2.2.0/core/src/main/scala/org/apache/spark/util/AccumulatorV2.scala#L370
[`Main`]: src/main/scala/com/foo/Main.scala
