package reductions

import common._
import org.scalameter._

object ParallelParenthesesBalancingRunner {

  @volatile var seqResult = false

  @volatile var parResult = false

  val standardConfig = config(
    Key.exec.minWarmupRuns -> 40,
    Key.exec.maxWarmupRuns -> 80,
    Key.exec.benchRuns -> 120,
    Key.verbose -> true
  ) withWarmer (new Warmer.Default)

  def main(args: Array[String]): Unit = {
    val length = 100000000
    val chars = new Array[Char](length)
    val threshold = 10000
    val seqtime = standardConfig measure {
      seqResult = ParallelParenthesesBalancing.balance(chars)
    }
    println(s"sequential result = $seqResult")
    println(s"sequential balancing time: $seqtime ms")

    val fjtime = standardConfig measure {
      parResult = ParallelParenthesesBalancing.parBalance(chars, threshold)
    }
    println(s"parallel result = $parResult")
    println(s"parallel balancing time: $fjtime ms")
    println(s"speedup: ${seqtime / fjtime}")
  }
}

object ParallelParenthesesBalancing {

  /** Returns `true` iff the parentheses in the input `chars` are balanced.
    */
  def balance(chars: Array[Char]): Boolean = {

    def loop(open: Int, pos: Int): Boolean = {

      if (pos == chars.length)
        return (open == 0)
      if (open < 0)
        return false

      val count = chars(pos) match {
        case '(' => 1
        case ')' => -1
        case _ => 0
      }

      loop(open + count, pos + 1)

    }

    loop(0, 0)

  }


  /** Returns `true` iff the parentheses in the input `chars` are balanced.
    */
  def parBalance(chars: Array[Char], threshold: Int): Boolean = {

    def traverse(from: Int, until: Int, leftCount: Int, rightCount: Int): (Int, Int) = {

      if (until == from) (leftCount, rightCount)
      else
        chars(from) match {
          case '(' => traverse(from + 1, until, leftCount + 1, rightCount)
          case ')' => traverse(from + 1, until, if (leftCount > 0) leftCount - 1 else leftCount, if (leftCount > 0) rightCount else rightCount + 1)
          case _ => traverse(from + 1, until, leftCount, rightCount)
        }
    }

    def reduce(from: Int, until: Int): (Int, Int) = {
      if (until - from <= threshold) traverse(from, until, 0, 0)
      else {

        val mid = until + (from - until) / 2

        val (leftRes, rightRes) = parallel(reduce(from, mid), reduce(mid, until))

        val matched = math.min(leftRes._1, rightRes._2)

        (leftRes._1 + rightRes._1 - matched, leftRes._2 + rightRes._2 - matched)
      }

    }

    reduce(0, chars.length) == (0, 0)
  }

  // For those who want more:
  // Prove that your reduction operator is associative!

}
