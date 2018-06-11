package observatory

import com.sksamuel.scrimage.{Image, Pixel}

import scala.math._

/**
  * 2nd milestone: basic visualization
  */
object Visualization {

  private val width = 360
  private val height = 180
  private val earthRadius = 6371
  private val power = 2
  private val alpha = 255

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location     Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {

    val tempWithDistance = temperatures.map { case (l, t) => (t, distance(l, location)) }

    val closePointTemperature: Option[Double] = tempWithDistance.find(_._2 <= 1).map(_._1)

    closePointTemperature.getOrElse {
      val tempWithReverseDistance = tempWithDistance.map { case (t, d) => (t, 1 / pow(d, power)) }
      val (d1, d2) = tempWithReverseDistance.foldLeft((0.0, 0.0))((sum, item) => (sum._1 + item._1 * item._2, sum._2 + item._2))
      d1 / d2
    }

  }


  private def distance(a: Location, b: Location): Double = {

    def isAntipodes(l1: Location, l2: Location): Boolean = l1.lat == -l2.lat && abs(l1.lon - l2.lon) == 180

    def radians(location: Location): (Double, Double) = (toRadians(location.lat), toRadians(location.lon))

    if (a == b) 0
    else {
      val angle = if (isAntipodes(a, b)) {
        Pi
      } else {
        val (f1, l1) = radians(a)
        val (f2, l2) = radians(b)
        acos(sin(f1) * sin(f2) + cos(f1) * cos(f2) * cos(abs(l1 - l2)))
      }
      earthRadius * angle
    }

  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value  The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color = {
    import scala.collection.Searching._

    val sorted: Array[(Temperature, Color)] = points.toArray.sortBy(_._1)

    if (value < sorted.head._1) sorted.head._2
    else if (value > sorted.last._1) sorted.last._2
    else {
      val i = sorted.search[(Temperature, Color)]((value, Color(0, 0, 0)))(Ordering.by[(Temperature, Color), Temperature](_._1)).insertionPoint
      val t1 = sorted(i)
      if (t1._1 == value) t1._2
      else {
        val t0 = sorted(i - 1)
        Color(
          linearInterpolation(t0._1, t0._2.red, t1._1, t1._2.red, value),
          linearInterpolation(t0._1, t0._2.green, t1._1, t1._2.green, value),
          linearInterpolation(t0._1, t0._2.blue, t1._1, t1._2.blue, value)
        )
      }
    }
  }

  private def linearInterpolation(x0: Temperature, y0: Int, x1: Temperature, y1: Int, x: Temperature): Int = {
    round(y0 + (y1-y0) * (x - x0) / (x1 - x0)).toInt
  }

  /**
    * @param temperatures Known temperatures
    * @param colors       Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {

    val pixels = new Array[Pixel](width * height)

    for (y <- 0 until height; x <- 0 until width) {
      val temperature = predictTemperature(temperatures, Location(90 - y, x - 180))
      val color = interpolateColor(colors, temperature)
      pixels.update(y * width + x, Pixel(color.red, color.green, color.blue, alpha))
    }

    Image(width, height, pixels)
  }

}

