package uk.gov.ons.addressIndex.server.utils
import javax.inject.Inject
import play.api.i18n.MessagesApi
import org.scalatest.{FlatSpec, Matchers}
import play.api.Logger
import uk.gov.ons.addressIndex.model.db.index.Relative
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.parsers.Tokens

import scala.collection.mutable.ListBuffer

/**
  * Unit tests for all the methods in the hopperScore calculation class
  */
class HopperScoreHelperTest extends FlatSpec with Matchers {

  val logger = Logger("HopperScoreHelperTest")

  val mockAddressTokens = Map (
    Tokens.buildingNumber -> "7",
    Tokens.paoStartNumber -> "7",
    Tokens.streetName -> "GATE REACH",
    Tokens.townName -> "EXETER",
    Tokens.postcode -> "PO7 6GA",
    Tokens.postcodeIn -> "6GA",
    Tokens.postcodeOut -> "PO7"
  )

  val mockPafAddress1 = AddressResponsePaf(
    udprn = "",
    organisationName = "",
    departmentName = "",
    subBuildingName = "",
    buildingName = "",
    buildingNumber = "7",
    dependentThoroughfare = "GATE REACH",
    thoroughfare = "",
    doubleDependentLocality = "",
    dependentLocality = "",
    postTown = "EXETER",
    postcode = "PO7 6GA",
    postcodeType = "",
    deliveryPointSuffix = "",
    welshDependentThoroughfare = "",
    welshThoroughfare = "",
    welshDoubleDependentLocality = "",
    welshDependentLocality = "",
    welshPostTown = "",
    poBoxNumber = "",
    startDate = "",
    endDate = ""
  )

  val mockNagAddress1 = AddressResponseNag(
    uprn = "",
    postcodeLocator = "PO7 6GA",
    addressBasePostal = "",
    usrn = "",
    lpiKey = "",
    pao = AddressResponsePao(
      paoText = "",
      paoStartNumber = "7",
      paoStartSuffix = "",
      paoEndNumber = "",
      paoEndSuffix = ""
    ),
    sao = AddressResponseSao(
      saoText = "",
      saoStartNumber = "",
      saoStartSuffix = "",
      saoEndNumber = "",
      saoEndSuffix = ""
    ),
    level = "",
    officialFlag = "",
    logicalStatus = "1",
    streetDescriptor = "",
    townName = "EXETER",
    locality = "",
    organisation = "",
    legalName = "",
    classificationCode = "R",
    localCustodianCode = "435",
    localCustodianName = "MILTON KEYNES",
    localCustodianGeogCode = "E06000042"
  )

  val mockRelative = Relative(
    level = 1,
    siblings = Array(6L, 7L),
    parents = Array(8L, 9L)
  )

  val mockRelativeResponse = AddressResponseRelative.fromRelative(mockRelative)

  val mockAddressResponseAddress = AddressResponseAddress(
    uprn = "",
    parentUprn = "",
    relatives = Seq(mockRelativeResponse),
    formattedAddress = "7, GATE REACH, EXETER, EX2 9GA",
    formattedAddressNag = "7, GATE REACH, EXETER, EX2 9GA",
    formattedAddressPaf = "7, GATE REACH, EXETER, EX2 9GA",
    paf = Some(mockPafAddress1),
    nag = Some(mockNagAddress1),
    geo = None,
    underlyingScore = 1.0f,
    objectScore = 0d,
    structuralScore = 0d,
    buildingScore = 0d,
    localityScore = 0d,
    unitScore = 0d,
    buildingScoreDebug = "0",
    localityScoreDebug = "0",
    unitScoreDebug = "0"
  )

  val mockAddressResponseAddressWithScores = AddressResponseAddress(
    uprn = "",
    parentUprn = "",
    relatives = Seq(mockRelativeResponse),
    formattedAddress = "7, GATE REACH, EXETER, EX2 9GA",
    formattedAddressNag = "7, GATE REACH, EXETER, EX2 9GA",
    formattedAddressPaf = "7, GATE REACH, EXETER, EX2 9GA",
    paf = Some(mockPafAddress1),
    nag = Some(mockNagAddress1),
    geo = None,
    underlyingScore = 1.0f,
    objectScore = -1.0d,
    structuralScore = 1.0d,
    buildingScore = 1.0d,
    localityScore = 1.0d,
    unitScore = -1.0d,
    buildingScoreDebug = "91",
    localityScoreDebug = "9111",
    unitScoreDebug = "0999"
  )

  val mockLocalityParams: ListBuffer[Tuple2[String, String]] =
    ListBuffer(("locality.9111", "EX2 6"), ("locality.9615", "EX1 1"), ("locality.9615", "EX1 1"),
      ("locality.9615", "EX1 1"), ("locality.9615", "EX1 1"), ("locality.9615", "EX1 1"), ("locality.9615", "EX1 1"),
      ("locality.9615", "EX1 3"), ("locality.9615", "EX1 3"), ("locality.9615", "EX1 3"))

  it should "calculate the ambiguity penalty for a given locality " in {
    // Given
    val localityScoreToTest = "locality.9615"
    val expected = 2.0d

    // When
    val actual = HopperScoreHelper.caluclateAmbiguityPenalty(localityScoreToTest, mockLocalityParams)

    // Then
    actual shouldBe expected
  }

  it should "capture the outcode from a postcode " in {
    // Given
    val postcodeToTest = "PO15 5RR"
    val expected = "PO15"

    // When
    val actual = HopperScoreHelper.getOutcode(postcodeToTest)

    // Then
    actual shouldBe expected
  }

  it should "capture the sector from a postcode " in {
    // Given
    val postcodeToTest = "PO15 5RR"
    val expected = "PO15 5"

    // When
    val actual = HopperScoreHelper.getSector(postcodeToTest)

    // Then
    actual shouldBe expected
  }

  it should "swap two digits in the incode of a postcode " in {
    // Given
    val postcodeToTest = "5RG"
    val expected = "5GR"

    // When
    val actual = HopperScoreHelper.swap(postcodeToTest, 1, 2)

    // Then
    actual shouldBe expected
  }

  it should "match two streets according to the rules " in {
    // Given
    val street1 = "HOPPER STREET"
    val street2 = "CHOPPER ROAD"
    val expected = 2

    // When
    val actual = HopperScoreHelper.matchStreets(street1, street2)

    // Then
    actual shouldBe expected
  }

  it should "match two building names according to the rules " in {
    // Given
    val building1 = "TONY'S TYRES"
    val building2 = "TONYS EXHAUSTS AND TYRES"
    val expected = 1

    // When
    val actual = HopperScoreHelper.matchNames(building1, building2)

    // Then
    actual shouldBe expected
  }

  it should "capture the start suffix from a building name or number " in {
    // Given
    val building1 = "72C-84E"
    val expected = "C"

    // When
    val actual = HopperScoreHelper.getStartSuffix(building1)

    // Then
    actual shouldBe expected
  }

  it should "capture the end suffix from a building name or number " in {
    // Given
    val building1 = "72C-84E"
    val expected = "E"

    // When
    val actual = HopperScoreHelper.getEndSuffix(building1)

    // Then
    actual shouldBe expected
  }

  it should "capture the top of the range of building numbers " in {
    // Given
    val building1 = "72C-84E"
    val expected = 84

    // When
    val actual = HopperScoreHelper.getRangeTop(building1)

    // Then
    actual shouldBe expected
  }

  it should "capture the bottom of the range of building numbers " in {
    // Given
    val building1 = "72C-84E"
    val expected = 72

    // When
    val actual = HopperScoreHelper.getRangeBottom(building1)

    // Then
    actual shouldBe expected
  }

  it should "calculate the minium of a list of numbers " in {
    // Given
    val expected = 42

    // When
    val actual = HopperScoreHelper.min(123, 42, 976, 996996)

    // Then
    actual shouldBe expected
  }

  it should "return the levenshtein edit distance of two strings " in {
    // Given
    val string1 = "BONGOES"
    val string2 = "BINGO"
    val expected = 3

    // When
    val actual = HopperScoreHelper.levenshtein(string1, string2)

    // Then
    actual shouldBe expected
  }

  /**
    * getScoresForAddresses
    * addScoresToAddress
    */

  it should "calculate the unit score for an address " in {
    // Given
    val expected = "unit.0828"

    // When
    val actual = HopperScoreHelper.calculateUnitScore(
      mockAddressResponseAddress,
      "UNIT 7",
      "@",
      "@",
      "@",
      "@",
      "@",
      "GATES")

    // Then
    actual shouldBe expected
  }

  it should "calculate the locality score for an address " in {
    // Given
    val expected = "locality.9111"

    // When
    val actual = HopperScoreHelper.calculateLocalityScore(
      mockAddressResponseAddress,
      "PO7 6GA",
      "PO7",
      "6GA",
      "@",
      "EXETER",
      "GATE REACH",
      "@",
      "@")

    // Then
    actual shouldBe expected
  }

  it should "calculate the building score for an address " in {
    // Given
    val expected = "building.71"

    // When
    val actual = HopperScoreHelper.calculateBuildingScore(
      mockAddressResponseAddress,
      "ONS",
      "7",
      "7",
      "@",
      "@",
      "@",
      "@")

    // Then
    actual shouldBe expected
  }

  it should "calculate the structural score for an address " in {
    // Given
    val buildingScore = 0.9d
    val localityScore = 0.25d
    val expected = 0.2250d

    // When
    val actual = HopperScoreHelper.calculateStructuralScore(buildingScore,localityScore)

    // Then
    actual shouldBe expected
  }

  it should "calculate the object score for an address " in {
    // Given
    val buildingScore = 0.9d
    val localityScore = 0.25d
    val unitScore = 0.5d
    val expected = 0.1125d

    // When
    val actual = HopperScoreHelper.calculateObjectScore(buildingScore,localityScore,unitScore)

    // Then
    actual shouldBe expected
  }

  it should "create a locality param List of Tuples from an address and tokens " in {
    // Given
    val expected = ("locality.9111","PO7 6")

    // When
    val actual = HopperScoreHelper.getLocalityParams(mockAddressResponseAddress,mockAddressTokens)

    // Then
    actual shouldBe expected
  }

  it should "get the scores for a address " in {
    // Given
    val expected = Seq(mockAddressResponseAddressWithScores)

    // When
    val actual = HopperScoreHelper.getScoresForAddresses(Seq(mockAddressResponseAddress),mockAddressTokens)

    // Then
    actual shouldBe expected
  }

  it should "add the scores for addresses to the response object " in {
    // Given
    val expected = mockAddressResponseAddressWithScores

    // When
    val actual = HopperScoreHelper.addScoresToAddress(mockAddressResponseAddress,mockAddressTokens,mockLocalityParams)

    // Then
    actual shouldBe expected
  }

}

