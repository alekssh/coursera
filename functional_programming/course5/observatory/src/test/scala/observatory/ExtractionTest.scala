package observatory

import java.time.LocalDate

import org.scalatest.{FunSuite, ShouldMatchers}

trait ExtractionTest extends FunSuite with ShouldMatchers {

  test("temperatures by location") {
    val temperatures = Extraction.locateTemperatures(2010, "/test-stations.csv", "/test-2010.csv")

    val expected = List(
      (LocalDate.of(2010, 1, 2), Location(59.792, 5.341), 10.0),
      (LocalDate.of(2010, 1, 3), Location(59.792, 5.341), 15.0),
      (LocalDate.of(2010, 1, 4), Location(61.383, 5.867), 12.0),
      (LocalDate.of(2010, 1, 5), Location(62.380,6), 0.0)
    )

    temperatures should contain theSameElementsAs expected
  }

  test("average by location") {

    val location1 = Location(1, 1)
    val location2 = Location(2, 2)

    val temperatures = List(
      (LocalDate.of(2010, 1, 1), location1, 10.0),
      (LocalDate.of(2010, 6, 1), location1, 25.0),
      (LocalDate.of(2010, 10, 1), location1, 7.0),
      (LocalDate.of(2010, 10, 1), location2, 12.0),
      (LocalDate.of(2010, 10, 1), location2, 23.0),
      (LocalDate.of(2010, 10, 1), location2, 1.0)
    )

    val averages = Extraction.locationYearlyAverageRecords(temperatures)

    averages should contain theSameElementsAs List((location1, 14.0), (location2, 12.0))
  }


}