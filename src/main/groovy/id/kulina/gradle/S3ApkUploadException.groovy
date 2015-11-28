package id.kulina.gradle

class S3ApkUploadException extends IOException {
  public S3ApkUploadException(final String message) {
    super(message)
  }
}
