package id.kulina.gradle

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project

public class S3ApkUploadPlugin implements Plugin<Project> {

  @Override
  void apply(final Project project) {
    final def log = project.logger
    final def hasAppPlugin = project.plugins.hasPlugin(AppPlugin)

    if (!hasAppPlugin) {
      throw new IllegalStateException("The 'com.android.application' plugin is required.")
    }

    final def extension = project.extensions.create("s3upload", S3ApkUploadExtension)

    project.android.applicationVariants.all { ApplicationVariant variant ->
      if (variant.buildType.debuggable) {
        log.debug("Skipping debuggable build type ${variant.buildType.name}")
        return
      }

      final def buildTypeName = variant.buildType.name.capitalize()

      final def productFlavourNames = variant.productFlavors.collect{ it.name.capitalize() }

      if (productFlavourNames.empty) {
        productFlavourNames = [""]
      }

      final def productFlavourName = productFlavourNames.join("")
      final def variationName = "${productFlavourName}${buildTypeName}"

      final def apkUploadTaskName = "S3ApkUpload${variationName}"

      final def apkUploadTask = project.tasks.create(apkUploadTaskName, S3ApkUploadTask)
      apkUploadTask.extension = extension
      apkUploadTask.variant = variant
      apkUploadTask.description = "Upload the APK for ${variationName} to S3"
    }
  }
}
