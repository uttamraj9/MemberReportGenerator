import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession, DataFrame, Row}
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import org.apache.spark.rdd.RDD

object MemberEligibilityReport {

  def main(args: Array[String]): Unit = {
    // Spark setup
    val conf = new SparkConf().setAppName("MemberEligibilityReport").setMaster("local[*]")
    val sc = new SparkContext(conf)
    val spark = SparkSession.builder.config(sc.getConf).getOrCreate()

    // Load datasets
    val memberEligibilityRDD = loadCSVAsRDD(sc, "/Users/uttamkumar/Desktop/Training/InteliJ/MemberReportGenerator/data/member_eligibility.csv")
    val memberMonthsRDD = loadCSVAsRDD(sc, "/Users/uttamkumar/Desktop/Training/InteliJ/MemberReportGenerator/data/member_months.csv")

    // Convert to DataFrames
    val memberEligibilityDF = toMemberEligibilityDF(spark, memberEligibilityRDD)
    val memberMonthsDF = toMemberMonthsDF(spark, memberMonthsRDD)

    // Join datasets on MemberID
    val joinedDF = memberMonthsDF.join(memberEligibilityDF, memberMonthsDF("MemberID") === memberEligibilityDF("ID"))

    // Report 1: Total member months per member
    val totalMemberMonthsPerMember = joinedDF
      .groupBy("MemberID", "FullName")
      .agg(count("*").alias("TotalMonths"))

    totalMemberMonthsPerMember
      .write
      .json("output/total_member_months_per_member")

    // Report 2: Total member months per member per month
    val totalMemberMonthsPerMonth = joinedDF
      .groupBy("MemberID", "Month")
      .agg(count("*").alias("TotalMonths"))

    totalMemberMonthsPerMonth
      .write
      .json("output/total_member_months_per_month")

    // Stop Spark session
    spark.stop()
    sc.stop()
  }

  def loadCSVAsRDD(sc: SparkContext, path: String): RDD[Array[String]] = {
    val lines = sc.textFile(path)
    val header = lines.first() // Get the header row

    lines
      .filter(line => line != header) // Skip the header
      .map(line => line.split(","))
  }

  def toMemberEligibilityDF(spark: SparkSession, rdd: RDD[Array[String]]): DataFrame = {
    val schema = StructType(
      Seq(
        StructField("ID", StringType, nullable = true),
        StructField("FullName", StringType, nullable = true),
        StructField("EligibilityDate", StringType, nullable = true)
      )
    )

    val rowRDD = rdd.map(arr => Row(arr(0), arr(1), arr(2)))

    spark.createDataFrame(rowRDD, schema)
  }

  def toMemberMonthsDF(spark: SparkSession, rdd: RDD[Array[String]]): DataFrame = {
    val schema = StructType(
      Seq(
        StructField("MemberID", StringType, nullable = true),
        StructField("Month", IntegerType, nullable = true),
        StructField("Year", IntegerType, nullable = true)
      )
    )

    val rowRDD = rdd.map(arr => Row(arr(0), arr(1).toInt, arr(2).toInt))

    spark.createDataFrame(rowRDD, schema)
  }
}
