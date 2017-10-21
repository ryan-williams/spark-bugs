import org.apache.spark.{ SparkConf, SparkContext }

abstract class App extends Serializable {
  // SparkContext stub
  @transient lazy val sc = new SparkContext(new SparkConf().setAppName("test").setMaster("local[4]").set("spark.ui.showConsoleProgress", "false"))

  // These fields get missed by the ClosureCleaner in some situations
  val n1 = 111
  val s1 = "aaa"

  // Simple scaffolding to exercise passing a closure to RDD.foreach in subclasses
  def rdd = sc.parallelize(1 to 1)
  def run(name: String): Unit = {
    print(s"$name:\t")
    body()
    sc.stop()
  }
  def body(): Unit = {}
}

object Main {
  /** [[App]]s generated this way will not correctly detect references to [[App.n1]] in Spark closures */
  val fn = () ⇒ new App {
    val n2 = 222
    val s2 = "bbb"
    override def body(): Unit = rdd.foreach { _ ⇒ println(s"$n1, $n2, $s1, $s2") }
  }

  /** Doesn't serialize closures correctly */
  val app1 = fn()

  /** Works fine */
  val app2 =
    new App {
      val n2 = 222
      val s2 = "bbb"
      override def body(): Unit = rdd.foreach { _ ⇒ println(s"$n1, $n2, $s1, $s2") }
    }

  /** [[App]]s created this way also work fine */
  def makeApp(): App =
    new App {
      val n2 = 222
      val s2 = "bbb"
      override def body(): Unit = rdd.foreach { _ ⇒ println(s"$n1, $n2, $s1, $s2") }
    }

  val app3 = makeApp()  // ok

  val fn2 = () ⇒ makeApp()  // ok

  def main(args: Array[String]): Unit = {
    fn().run("fn")    // bad: n1 → 0, s1 → null
    app1.run("app1")  // bad: n1 → 0, s1 → null
    app2.run("app2")  // ok
    app3.run("app3")  // ok
    fn2().run("fn2")  // ok
  }
}
