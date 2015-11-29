package id.kulina.gradle

import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.build.gradle.internal.variant.ApkVariantData
import com.android.builder.model.BuildType
import org.gradle.api.Action
import org.gradle.api.Project

val BuildType.debuggable: Boolean
  get() = this.isDebuggable

val ApplicationVariant.variantData : ApkVariantData ?
  get()  =
    if (this is ApplicationVariantImpl ) {
      val clazz = ApplicationVariantImpl::class.java
      val m = clazz.getDeclaredMethod("getVariantData")
      m.isAccessible = true
      m.invoke(this) as ApkVariantData
    }
    else null


internal class S3ApkPluginOperations (val project : Project,
                                      val ext: S3ApkUploadExtension)
: Action<ApplicationVariant> {

  override fun execute(variant: ApplicationVariant) {
    val log = project.logger
    if(variant.buildType.debuggable) {
      log.debug("Skipping debuggable build type ${variant.buildType.name}")
      return
    }

    val buildTypeName = variant.buildType.name.capitalize()
    val productFlavourNames = variant.productFlavors.map { it.name.capitalize() }
    val productFlavourName = if (productFlavourNames.isEmpty()) "" else
      productFlavourNames.joinToString("")
    val variationName = "$productFlavourName$buildTypeName"
    val outputData = variant.outputs.first() as ApkVariantOutput
    val variantData = variant.variantData
    val zipAlignTask = outputData.zipAlign
    val apkUploadTaskName = "S3ApkUpload$variationName"

    if (zipAlignTask != null && variantData?.zipAlignEnabled?:false) {
      val apkUploadTask = project.tasks.create(apkUploadTaskName, S3ApkUploadTask::class.java)
      apkUploadTask.extension = ext
      apkUploadTask.variant = variant
      apkUploadTask.description = "Upload the APK for $variationName to S3"
    }
    else {
      log.warn("Could not find zipAlign task, Did you specify signingConfig correctly for $variationName")
    }
  }
}