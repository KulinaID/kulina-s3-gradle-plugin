package id.kulina.gradle

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.logging.Logger

class S3ApkUploadTask extends DefaultTask {

  ApplicationVariant variant
  S3ApkUploadExtension extension
  Logger logger

  @TaskAction
  s3UploadApk() {
    final def apkOutput = variant.outputs.find { final BaseVariantOutput vo -> vo instanceof ApkVariantOutput }
    final def apkFile = apkOutput.outputFile

    final def s3Client = new AmazonS3Client()
    final def bucketName = extension.bucketName

    if (!bucketName || bucketName.isEmpty()) {
      throw new S3ApkUploadException("s3ApkUpload.bucketName cannot be empty or null")
    }
    final def targetPath = extension.path
    if (targetPath.length() > 0 && !targetPath.endsWith("/")) {
      targetPath = extension.path + "/"
    }

    final def objectKey = "${targetPath}${apkFile.name}"

    final def putObjectReq = new PutObjectRequest(bucketName, objectKey, apkFile)

    try {
      println "Uploading ${apkFile.name} to ${objectKey} ..."
      s3Client.putObject(putObjectReq)
      println "Succeeded!"
    }
    catch (final AmazonServiceException serviceExc) {
      final String message = "Rejected upload by Amazon S3:[" +
          "Error Message: ${serviceExc.errorMessage}, Status: ${serviceExc.statusCode}, " +
          "AWS Error Code: ${serviceExc.errorCode}, Error Type: ${serviceExc.errorType}, " +
          "S3 Request Id: ${serviceExc.requestId}"
      println "Failed!"
      throw new S3ApkUploadException(message)
    }
    catch (final AmazonClientException clientExc) {
      println "Failed!"
      throw new S3ApkUploadException(clientExc.message)
    }
  }
}
