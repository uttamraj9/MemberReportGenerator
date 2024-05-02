import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.{Files, Paths}

class MemberEligibilityReportTest extends AnyFlatSpec with Matchers {

  var spark: SparkSession = _
  var sc: SparkContext = _

  def setup(): Unit = {
    // Setup Spark context and session
    val conf = new SparkConf().setAppName("TestMemberEligibilityReport").setMaster("local[*]")
    sc = new SparkContext(conf)
    spark = SparkSession.builder.config(sc.getConf).getOrCreate()
  }

  def teardown(): Unit = {
    spark.stop()
    sc.stop()
  }

  "MemberEligibilityReport" should "generate total member months per member correctly" in {
    setup()
    val spark2 = spark
    import spark2.implicits._

    // Mock DataFrames
    val memberEligibilityDF = createMockEligibilityDF()
    val memberMonthsDF = createMockMonthsDF()

    // Join datasets on MemberID
    val joinedDF = memberMonthsDF.join(memberEligibilityDF, memberMonthsDF("MemberID") === memberEligibilityDF("ID"))

    // Report 1: Total member months per member
    val totalMemberMonthsPerMember = joinedDF
      .groupBy("MemberID", "FullName")
      .agg(count("*").alias("TotalMonths"))

    totalMemberMonthsPerMember.write.json("test_output/total_member_months_per_member")

    // Check the saved file exists
    assert(Files.exists(Paths.get("test_output/total_member_months_per_member")))

    teardown()
  }

  "MemberEligibilityReport" should "generate total member months per member per month correctly" in {
    setup()

    val spark2 = spark
    import spark2.implicits._

    // Mock DataFrames
    val memberEligibilityDF = createMockEligibilityDF()
    val memberMonthsDF = createMockMonthsDF()

    // Join datasets on MemberID
    val joinedDF = memberMonthsDF.join(memberEligibilityDF, memberMonthsDF("MemberID") === memberEligibilityDF("ID"))

    // Report 2: Total member months per member per month
    val totalMemberMonthsPerMonth = joinedDF
      .groupBy("MemberID", "Month")
      .agg(count("*").alias("TotalMonths"))

    totalMemberMonthsPerMonth.write.json("test_output/total_member_months_per_month")

    // Check the saved file exists
    assert(Files.exists(Paths.get("test_output/total_member_months_per_month")))

    teardown()
  }

  def createMockEligibilityDF(): DataFrame = {
    val eligibilityData = Seq(
      ("1", "John Doe", "2022-01-01"),
      ("2", "Jane Smith", "2021-12-15")
    )

    val schema = StructType(
      Seq(
        StructField("ID", StringType, nullable = true),
        StructField("FullName", StringType, nullable = true),
        StructField("EligibilityDate", StringType, nullable = true)
      )
    )

    val rowRDD = spark.sparkContext.parallelize(
      eligibilityData.map {
        case (id, name, date) => Row(id, name, date)
      }
    )

    spark.createDataFrame(rowRDD, schema)
  }

  def createMockMonthsDF(): DataFrame = {
    val monthsData = Seq(
      ("1", 1, 2022),
      ("1", 2, 2022),
      ("2", 12, 2021)
    )

    val schema = StructType(
      Seq(
        StructField("MemberID", StringType, nullable = true),
        StructField("Month", IntegerType, nullable = true),
        StructField("Year", IntegerType, nullable = true)
      )
    )

    val rowRDD = spark.sparkContext.parallelize(
      monthsData.map {
        case (id, month, year) => Row(id, month, year)
      }
    )

    spark.createDataFrame(rowRDD, schema)
  }
}
