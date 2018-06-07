package observatory

import java.time.LocalDate

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._


/**
  * 1st milestone: data extraction
  */
object Extraction {

  val spark: SparkSession = SparkSession.builder().appName("observatory").config("spark.master", "local").getOrCreate()

  import spark.implicits._

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {

    val stationsSchema = StructType(List(
      StructField("stn", IntegerType, true),
      StructField("wban", IntegerType, true),
      StructField("lat", DoubleType, true),
      StructField("lon", DoubleType, true)
    ))

    val stations = spark.read.schema(stationsSchema).csv(path(stationsFile)).
      where(($"stn".isNotNull || $"wban".isNotNull) && $"lat".isNotNull && $"lon".isNotNull).na.fill(Map("stn" -> -1, "wban" -> -1))

    val temperaturesSchema = StructType(List(
      StructField("stn", IntegerType, true),
      StructField("wban", IntegerType, true),
      StructField("month", IntegerType, true),
      StructField("day", IntegerType, true),
      StructField("temperature", DoubleType, true)
    ))

    val temperatures = spark.read.schema(temperaturesSchema).csv(path(temperaturesFile))
      .where(($"stn".isNotNull || $"wban".isNotNull) && $"temperature".isNotNull && $"temperature" =!= 9999.9).na.fill(Map("stn" -> -1, "wban" -> -1))

    val joined = temperatures.join(stations, Seq("stn", "wban"))
      .select("month", "day", "lat", "lon", "temperature")

    joined.show()

    val transformed = joined.rdd.map(row =>
      (LocalDate.of(year, row.getInt(0), row.getInt(1)), Location(row.getDouble(2), row.getDouble(3)), toCelcium(row.getDouble(4)))
    )

    transformed.collect()
  }

  private def toCelcium(fahreinheit: Double): Double = (fahreinheit - 32) * 5.0 / 9

  private def path(filePath: String): String = getClass.getResource(filePath).toURI.toString

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    spark.sparkContext.parallelize(records.toSeq).map(row => (row._2, (row._3, 1)))
      .reduceByKey((v1, v2) => (v1._1 + v2._1, v1._2 + v2._2)).mapValues { case (sum, count) => sum / count }.collect()
  }

  def main(args: Array[String]): Unit = {
    val temperatures = locateTemperatures(2010, "/stations.csv", "/2010.csv")

    val averages = locationYearlyAverageRecords(temperatures)

    averages.foreach(print)
  }

}
