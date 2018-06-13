package observatory

import com.sksamuel.scrimage.{Image, Pixel}

import scala.math.{log, pow, round}

/**
  * 5th milestone: value-added information visualization
  */
object Visualization2 {
  private val width, height = 256

  private val alpha = 127

  val pixZoom = round(log(width) / log(2)).toInt

  /**
    * @param point (x, y) coordinates of a point in the grid cell
    * @param d00   Top-left value
    * @param d01   Bottom-left value
    * @param d10   Top-right value
    * @param d11   Bottom-right value
    * @return A guess of the value at (x, y) based on the four known values, using bilinear interpolation
    *         See https://en.wikipedia.org/wiki/Bilinear_interpolation#Unit_Square
    */
  def bilinearInterpolation(
                             point: CellPoint,
                             d00: Temperature,
                             d01: Temperature,
                             d10: Temperature,
                             d11: Temperature
                           ): Temperature = {
    d00 * (1 - point.x) * (1 - point.y) + d10 * point.x * (1 - point.y) + d01 * (1 - point.x) * point.y + d11 * point.x * point.y
  }


  /**
    * @param grid   Grid to visualize
    * @param colors Color scale to use
    * @param tile   Tile coordinates to visualize
    * @return The image of the tile at (x, y, zoom) showing the grid using the given color scale
    */
  def visualizeGrid(
                     grid: GridLocation => Temperature,
                     colors: Iterable[(Temperature, Color)],
                     tile: Tile
                   ): Image = {
    val offsetX: Int = tile.x * pow(2, pixZoom).toInt

    val offsetY: Int = tile.y * pow(2, pixZoom).toInt

    val subTiles = for (y <- 0 until width; x <- 0 until height) yield Tile(x + offsetX, y + offsetY, tile.zoom + pixZoom)

    val pixels = subTiles.map(Interaction.tileLocation).map { loc =>

      val latFloor = loc.lat.floor.toInt
      val latCeil = loc.lat.ceil.toInt
      val lonFloor = loc.lon.floor.toInt
      val lonCeil = loc.lon.ceil.toInt

      bilinearInterpolation(CellPoint(loc.lon - lonFloor, loc.lat - latFloor),
        grid(GridLocation(latFloor, lonFloor)),
        grid(GridLocation(latCeil, lonFloor)),
        grid(GridLocation(latFloor, lonCeil)),
        grid(GridLocation(latCeil, lonCeil))
      )

    }.map(Visualization.interpolateColor(colors, _)).map(color => Pixel(color.red, color.green, color.blue, alpha))

    Image(width, height, pixels.toArray)

  }

}
