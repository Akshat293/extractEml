import java.io.File
import java.nio.file.Paths
import org.apache.avro.Schema
import org.apache.avro.file.DataFileWriter
import org.apache.avro.generic.{GenericData, GenericDatumWriter}
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{PutObjectRequest, S3Exception}

object AvroUploader {
  def main(args: Array[String]): Unit = {
    val bucketName = "your-bucket-name"
    val keyName = "path/to/your/avro-file.avro"
    val record = createSampleRecord()

    // Serialize record to Avro file
    val avroFile = new File("sample.avro")
    val schema = record.getSchema
    val datumWriter = new GenericDatumWriter[GenericData.Record](schema)
    val dataFileWriter = new DataFileWriter[GenericData.Record](datumWriter)

    var s3Client: S3Client = null

    try {
      dataFileWriter.create(schema, avroFile)
      dataFileWriter.append(record)
      dataFileWriter.close()

      // Upload Avro file to S3
      s3Client = S3Client.builder()
        .credentialsProvider(DefaultCredentialsProvider.create())
        .region(Region.US_EAST_1) // Replace with your desired region
        .build()

      val fileObject = Paths.get(avroFile.getPath)
      val putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(keyName)
        .build()

      s3Client.putObject(putObjectRequest, fileObject)
      println(s"Uploaded Avro file to S3 bucket $bucketName with key $keyName")

    } catch {
      case e: S3Exception => e.printStackTrace()
      case e: Exception => e.printStackTrace()
    } finally {
      // Clean up local Avro file
      if (avroFile.exists()) {
        if (avroFile.delete()) {
          println(s"Deleted local file: ${avroFile.getPath}")
        } else {
          println(s"Failed to delete local file: ${avroFile.getPath}")
        }
      }
      if (s3Client != null) {
        s3Client.close()
      }
    }
  }

  def createSampleRecord(): GenericData.Record = {
    val schemaStr =
      """
        |{
        |  "type": "record",
        |  "name": "SampleRecord",
        |  "fields": [
        |    {"name": "id", "type": "int"},
        |    {"name": "name", "type": "string"},
        |    {"name": "age", "type": "int"}
        |  ]
        |}
        |""".stripMargin

    val schema = new Schema.Parser().parse(schemaStr)
    val record = new GenericData.Record(schema)
    record.put("id", 1)
    record.put("name", "John Doe")
    record.put("age", 30)
    record
  }
}
