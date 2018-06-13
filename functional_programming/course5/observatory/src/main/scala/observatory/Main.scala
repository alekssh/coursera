package observatory

import java.nio.file.{Files, Paths}

object Main extends App {

  //generateTemperatures()
  generateDeviations()

  def generateTemperatures() = {

    for (year <- 1975 to 1980) {
      val temperatures = Extraction.locateTemperatures(year, "/stations.csv", s"/${year}.csv")

      val locationToTemperature = Extraction.locationYearlyAverageRecords(temperatures)

      Interaction.generateTiles[AnyRef](
        List((year, None)),
        (year: Year, tile: Tile, data: AnyRef) => {
          val path = Paths.get(s"${Config.outputDir}/${LayerName.Temperatures.id}/${year}/${tile.zoom}/${tile.x}-${tile.y}.png")
          Files.createDirectories(path.getParent)

          Interaction.tile(locationToTemperature, Colors.temperatures(), tile).output(path)

          //Visualization2.visualizeGrid(Manipulation.makeGrid(locationToTemperature), colors, tile).output(path)
        }
      )

    }
  }


  def generateDeviations() {


    val temperaturess = for (year <- Config.normalsRange)
      yield Extraction.locationYearlyAverageRecords(Extraction.locateTemperatures(year, "/stations.csv", s"/${year}.csv"))

    val normals = Manipulation.average(temperaturess)

    for (year <- Config.deviationsRange) {

      val deviations = Manipulation.deviation(
        Extraction.locationYearlyAverageRecords(Extraction.locateTemperatures(year, "/stations.csv", s"/${year}.csv")),
        normals)

      Interaction.generateTiles[AnyRef](
        List((year, None)),
        (year: Year, tile: Tile, data: AnyRef) => {
          val path = Paths.get(s"${Config.outputDir}/${LayerName.Deviations.id}/${year}/${tile.zoom}/${tile.x}-${tile.y}.png")
          Files.createDirectories(path.getParent)
          Visualization2.visualizeGrid(deviations, Colors.deviations(), tile).output(path)
        }
      )

    }
  }

}
