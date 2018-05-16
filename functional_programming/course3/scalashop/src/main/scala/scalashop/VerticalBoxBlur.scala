package scalashop

import common._
import org.scalameter._

object VerticalBoxBlurRunner {

  val standardConfig = config(
    Key.exec.minWarmupRuns -> 5,
    Key.exec.maxWarmupRuns -> 10,
    Key.exec.benchRuns -> 10,
    Key.verbose -> true
  ) withWarmer (new Warmer.Default)

  def main(args: Array[String]): Unit = {
    val radius = 3
    //    val width = 1920
    val width = 10
    //    val height = 1080
    val height = 10
    val src = new Img(width, height)
    val dst = new Img(width, height)
    val seqtime = standardConfig measure {
      VerticalBoxBlur.blur(src, dst, 0, width, radius)
    }
    println(s"sequential blur time: $seqtime ms")

    //val numTasks = 32
    val numTasks = 3
    val partime = standardConfig measure {
      VerticalBoxBlur.parBlur(src, dst, numTasks, radius)
    }
    println(s"fork/join blur time: $partime ms")
    println(s"speedup: ${seqtime / partime}")
  }

}

/** A simple, trivially parallelizable computation. */
object VerticalBoxBlur {

  /** Blurs the columns of the source image `src` into the destination image
    * `dst`, starting with `from` and ending with `end` (non-inclusive).
    *
    * Within each column, `blur` traverses the pixels by going from top to
    * bottom.
    */
  def blur(src: Img, dst: Img, from: Int, end: Int, radius: Int): Unit = {
    for (x <- from until end; y <- 0 until src.height)
      dst.update(x, y, boxBlurKernel(src, x, y, radius))
  }

  /** Blurs the columns of the source image in parallel using `numTasks` tasks.
    *
    * Parallelization is done by stripping the source image `src` into
    * `numTasks` separate strips, where each strip is composed of some number of
    * columns.
    */
  def parBlur(src: Img, dst: Img, numTasks: Int, radius: Int): Unit = {
    val bulkSize: Int = if (numTasks > src.width) 1 else math.ceil(src.width.toDouble / numTasks).toInt

    val intervals = for (i <- 0 until src.width by bulkSize) yield (i, math.min((i + bulkSize), src.width))

    intervals.map(interval => task {
      blur(src, dst, interval._1, interval._2, radius)
    }).foreach(_.join())

  }

}
