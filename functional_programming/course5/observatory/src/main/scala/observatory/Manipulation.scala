package observatory

import scala.collection.mutable

/**
  * 4th milestone: value-added information
  */
object Manipulation {

  /**
    * @param temperatures Known temperatures
    * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
    *         returns the predicted temperature at this location
    */
  def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {
    val grid = new mutable.HashMap[GridLocation, Temperature]
    (gridLocation: GridLocation) =>
      grid.getOrElseUpdate(gridLocation,
        Visualization.predictTemperature(temperatures, gridLocation)
      )
  }


  /**
    * @param temperaturess Sequence of known temperatures over the years (each element of the collection
    *                      is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperaturess: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {

    val gridAverages = new mutable.HashMap[GridLocation, Temperature]
    val gridTemperatures = temperaturess.map(makeGrid)

    (gridLocation: GridLocation) => {
      gridAverages.getOrElseUpdate(gridLocation, {
        val locationTemperatures = gridTemperatures.map(_ (gridLocation))
        locationTemperatures.sum / locationTemperatures.size
      }
      )
    }
  }

  private implicit def toLocation(gridLocation: GridLocation): Location = Location(gridLocation.lat, gridLocation.lon)

  /**
    * @param temperatures Known temperatures
    * @param normals      A grid containing the “normal” temperatures
    * @return A grid containing the deviations compared to the normal temperatures
    */
  def deviation(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): GridLocation => Temperature = {
    val deviations = new mutable.HashMap[GridLocation, Temperature]
    val grid = makeGrid(temperatures)
    (gridLocation: GridLocation) => deviations.getOrElseUpdate(gridLocation, grid(gridLocation) - normals(gridLocation))
  }


}

