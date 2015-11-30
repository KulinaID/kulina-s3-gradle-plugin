package id.kulina.gradle

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.IOException

open class S3ApkUploadExtension {
  var bucketName: String? = null
  var path: String = ""
}
class S3ApkUploadException(message: String) : IOException(message)

open class S3ApkUploadTask : DefaultTask() {
  lateinit var variant: ApplicationVariant
  lateinit var extension: S3ApkUploadExtension

  @TaskAction
  fun s3UploadTask() {
    val apkOutput = variant.outputs.find { it is ApkVariantOutput }
    val apkFile = apkOutput!!.outputFile
    val s3Client = AmazonS3Client()

    val bucketName = extension.bucketName ?: throw S3ApkUploadException("s3ApkUpload.bucketName cannot be null")
    val noTrailingSlash = extension.path.length > 0 && !extension.path.endsWith("/")
    val targetPath = if (noTrailingSlash) "${extension.path}/" else extension.path
    val objectKey = "$targetPath${apkFile.name}"
    val putObjectReq = PutObjectRequest(bucketName, objectKey, apkFile)

    try {
      println("Uploading ${apkFile.name} to $objectKey ...")
      s3Client.putObject(putObjectReq)
      println("Succeeded!")
    }
    catch(serviceExc: AmazonServiceException) {
      println("Failed")
      val message = "Rejected upload by Amazon S3:[" +
          "Error Message: ${serviceExc.errorMessage}, Status: ${serviceExc.statusCode}, " +
          "AWS Error Code: ${serviceExc.errorCode}, Error Type: ${serviceExc.errorType}, " +
          "S3 Request Id: ${serviceExc.requestId}"
      throw S3ApkUploadException(message)
    }
    catch(e: AmazonClientException) {
      println("Failed")
      throw S3ApkUploadException(e.message?:"Failure on S3 Client Request")
    }
  }
}
