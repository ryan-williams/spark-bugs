# ClosureCleaner bug

In some situations, `ClosureCleaner` fails to include superclass fields referenced in closures passed to RDD operations.

## Run

```bash
$ sbt run
…
fn:	0, 222, null, bbb
app1:	0, 222, null, bbb
app2:	111, 222, aaa, bbb
app3:	111, 222, aaa, bbb
fn2:	111, 222, aaa, bbb
```

See [Main.scala](src/main/scala/Main.scala).

The first two versions, `fn` and `app1`, have superclass fields erroneously nulled out.

`App`s created from this function have this problem:

```scala
val fn = () ⇒ new App {
  val n2 = 222
  val s2 = "bbb"
  override def body(): Unit = rdd.foreach { 
    _ ⇒ println(s"$n1, $n2, $s1, $s2") 
  }
}

val app1 = fn()  // bad
```

A slight rearrangement works fine:

```scala
def makeApp(): App = new App {
  val n2 = 222
  val s2 = "bbb"
  override def body(): Unit = rdd.foreach { 
    _ ⇒ println(s"$n1, $n2, $s1, $s2") 
  }
}

val app3 = makeApp()      // ok
val fn2 = () ⇒ makeApp()  // ok
```

The `new App { … }` is identical in both cases, but factoring it out hides the issue (`makeApp`/`fn` demonstrate this); Something specific to the `() => new App { … }` syntax causes the problem.
