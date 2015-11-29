package id.kulina.gradle

import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

public class S3ApkUploadPlugin implements Plugin<Project> {

  @Override
  void apply(final Project project) {

    if (!project.plugins.hasPlugin(AppPlugin)) {
      throw new IllegalStateException("The 'com.android.application' plugin is required.")
    }

    final def extension = project.extensions.create("s3upload", S3ApkUploadExtension)
    project.android.applicationVariants.all(new S3ApkPluginOperations(project, extension))
  }
}