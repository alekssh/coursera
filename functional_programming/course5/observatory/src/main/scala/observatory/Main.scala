package observatory

object Main extends App {


  val temperatures = Extraction.locateTemperatures(2010, "/stations.csv", "/2010.csv")

  val locationToTemperature = Extraction.locationYearlyAverageRecords(temperatures)

  val temperatureToColor = List(
    (60.0, Color(255, 255, 555)),
    (32.0, Color(255, 0, 0)),
    (12.0, Color(255, 255, 0)),
    (0.0, Color(0, 255, 555)),
    (-15.0, Color(0, 0, 555)),
    (-27.0, Color(255, 0, 555)),
    (-50.0, Color(33, 0, 107)),
    (-60.0, Color(0, 0, 0))
  )

  val image = Visualization.visualize(locationToTemperature, temperatureToColor)

  image.output("target/image.png")

}
