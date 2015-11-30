# Overview

This plugin allows you to upload release APKs to S3 bucket. 

# Usage

```groovy
buildscript {
  repositories {
    jcenter()
  }
  dependencies {
    classpath 'id.kulina.gradle:kulina-gradle-s3-plugin:1.1.2'
  }
}

apply plugin: 'id.kulina.gradle'

```

You'd need to set the acces key id, secret, and region in the environment variable to be able to use this plugin.

## Configuration

There are only two options `bucketName` which is mandatory and `path` which is optional. The default will upload to
the root of the bucket.


```groovy
s3upload {
  bucketName = 'your-bucket-name'
  path = '/path/to/artifact'
}

```

# Licensing

This software is licensed under MIT license


Copyright (c) 2015 Kulina
