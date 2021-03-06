# spark-bugs
Dumping ground for repros of issues I find with Apache Spark

Specific repros can be found in branches:

- [SPARK-16599](https://issues.apache.org/jira/browse/SPARK-16599) ([`msc`](https://github.com/ryan-williams/spark-bugs/tree/msc)): various problems with multiple-`SparkContext` support
- [SPARK-21143](https://issues.apache.org/jira/browse/SPARK-21143) ([`netty`](https://github.com/ryan-williams/spark-bugs/tree/netty)): multiple Netty versions in transitive-dependencies result in hanging app
- [SPARK-21425](https://issues.apache.org/jira/browse/SPARK-21425) ([`accum`](https://github.com/ryan-williams/spark-bugs/tree/accum)): static `Accumulator`s considered harmful
- [SPARK-21569](https://issues.apache.org/jira/browse/SPARK-21569) ([`hf`](https://github.com/ryan-williams/spark-bugs/tree/hf)): missing internal-class Kryo-registration
- [SPARK-22288](https://issues.apache.org/jira/browse/SPARK-22288) ([`serde`](https://github.com/ryan-williams/spark-bugs/tree/serde)): `no valid constructor` deserialization error
- [SPARK-22328](https://issues.apache.org/jira/browse/SPARK-22328) ([`closure`](https://github.com/ryan-williams/spark-bugs/tree/closure)): `ClosureCleaner` misses superclass fields, they end up as `null` in closure.
