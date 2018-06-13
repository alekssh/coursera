package observatory

import com.sksamuel.scrimage.{Image, Pixel}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.math._

/**
  * 3rd milestone: interactive visualization
  */
object Interaction {

  private val width, height = 256

  private val final_width, final_height = 256

  private val alpha = 127

  val timeout = 36000 seconds

  val pixZoom = round(log(width) / log(2)).toInt

  /**
    *
    * @param tile Tile coordinates
    * @return The latitude and longitude of the top-left corner of the tile, as per http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    */
  def tileLocation(tile: Tile): Location = {
    val n = pow(2, tile.zoom)
    val lat = toDegrees(atan(sinh(Pi * (1 - 2 * tile.y / n))))
    val lon = tile.x / n * 360.0 - 180
    Location(lat, lon)
  }

  /**
    * @param temperatures Known temperatures
    * @param colors       Color scale
    * @param tile         Tile coordinates
    * @return A 256×256 image showing the contents of the given tile
    */
  def tile(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)], tile: Tile): Image = {

    val offsetX: Int = tile.x * pow(2, pixZoom).toInt
    val offsetY: Int = tile.y * pow(2, pixZoom).toInt

    val subTiles = for (y <- 0 until width; x <- 0 until height) yield Tile(x + offsetX, y + offsetY, tile.zoom + pixZoom)

    val pixels = subTiles.map(tileLocation).map(Visualization.predictTemperature(temperatures, _))
      .map(Visualization.interpolateColor(colors, _)).map(color => Pixel(color.red, color.green, color.blue, alpha))

    Image(width, height, pixels.toArray).scaleTo(final_width, final_height)
  }

  /**
    * Generates all the tiles for zoom levels 0 to 3 (included), for all the given years.
    *
    * @param yearlyData    Sequence of (year, data), where `data` is some data associated with
    *                      `year`. The type of `data` can be anything.
    * @param generateImage Function that generates an image given a year, a zoom level, the x and
    *                      y coordinates of the tile and the data to build the image from
    */
  def generateTiles[Data](
                           yearlyData: Iterable[(Year, Data)],
                           generateImage: (Year, Tile, Data) => Unit
                         ): Unit = {

    implicit val ec = scala.concurrent.ExecutionContext.global

    val futures = for {
      yearData <- yearlyData
      zoom <- 0 to 3
      y <- 0 until pow(2, zoom).toInt
      x <- 0 until pow(2, zoom).toInt
    } yield Future(generateImage(yearData._1, Tile(x, y, zoom), yearData._2))

    Await.ready(Future.sequence(futures), timeout)

  }

}
