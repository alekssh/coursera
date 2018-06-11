package observatory

import java.nio.file.{Files, Paths}

object Main extends App {

  val year = 2015

  val temperatures = Extraction.locateTemperatures(year, "/stations.csv", s"/${year}.csv")

  val locationToTemperature = Extraction.locationYearlyAverageRecords(temperatures)

  val colors = List(
    (60.0, Color(255, 255, 555)),
    (32.0, Color(255, 0, 0)),
    (12.0, Color(255, 255, 0)),
    (0.0, Color(0, 255, 555)),
    (-15.0, Color(0, 0, 555)),
    (-27.0, Color(255, 0, 555)),
    (-50.0, Color(33, 0, 107)),
    (-60.0, Color(0, 0, 0))
  )

  //val image = Visualization.visualize(locationToTemperature, colors)
  //image.output("target/image.png")


  Interaction.generateTiles[AnyRef](
    List((year, None)),
    (year: Year, tile: Tile, data: AnyRef) => {
      val path = Paths.get(s"target/temperatures/${year}/${tile.zoom}/${tile.x}-${tile.y}.png")
      Files.createDirectories(path.getParent)
      Interaction.tile(locationToTemperature, colors, tile).output(path)
    }
  )

}
