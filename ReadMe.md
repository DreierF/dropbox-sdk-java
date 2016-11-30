# Inofficial Android client library for the Dropbox API.

An android library to access [Dropbox's HTTP-based Core API v2](https://www.dropbox.com/developers/documentation/http/documentation). This SDK also supports the older [Core API v1](https://www.dropbox.com/developers-v1/core/docs), but that support will be removed at some point.

This is a fork of the official Dropbox Java API, with the aim to reduce the method count of the library.
Currently it has been shrinked to 5789 methods.
This has been _removed_:
  * Team features
  * Sharing features
  * Google App Engine support
  * OkHttp support (OkHttp3 support is still included)
  * OSGi

Further changes:
  * Adopted to the standard library layout
  * Included consumer proguard file
  * Changed language level to Java 7

License: [MIT](License.txt)

[Javadoc.](https://dropbox.github.io/dropbox-sdk-java/api-docs/v2.1.x/)

## Setup

If you are using Gradle, then edit your project's "build.gradle" and add this to the `dependencies` section:

```groovy
dependencies {
    // ...
    compile 'com.github.dreierf:dropbox-android-core:2.1.2'
}
```

You can also download the Java SDK JAR and and its required dependencies directly from the [latest release page](https://github.com/dropbox/dropbox-sdk-java/releases/latest). Note that the distribution artifacts on the releases pages do not contain optional dependencies.

## Get a Dropbox API key

You need a Dropbox API key to make API requests.
  * Go to: [https://www.dropbox.com/developers/apps](https://www.dropbox.com/developers/apps)
  * If you've already registered an app, click on the "Options" link to see the app's API key and secret.
  * Otherwise, click "Create an app" to register an app.  Choose "Dropbox API app", then "Files and datastores", then "Yes" or "No" [depending on your needs](https://www.dropbox.com/developers/reference#permissions).

Save the API key to a JSON file called, say, "test.app":

```
{
  "key": "Your Dropbox API app key",
  "secret": "Your Dropbox API app secret"
}
```

## Using the Dropbox API

Before your app can access a Dropbox user's files, the user must authorize your application using OAuth 2.  Successfully completing this authorization flow gives you an _access token_ for the user's Dropbox account, which grants you the ability to make Dropbox API calls to access their files.
Example for an Android app: [Android example](examples/android/src/main/java/com/dropbox/core/examples/android/UserActivity.java)

Once you have an access token, create a [`DbxClientV2`](https://dropbox.github.io/dropbox-sdk-java/api-docs/v2.1.x/com/dropbox/core/v2/DbxClientV2.html) and start making API calls.

You only need to perform the authorization process once per user.  Once you have an access token for a user, save it somewhere persistent, like in a database.  The next time that user visits your app's, you can skip the authorization process and go straight to creating a `DbxClientV2` and making API calls.

## Building from source

```
git clone https://github.com/dropbox/dropbox-sdk-java.git
cd dropbox-sdk-java
./update-submodules    # also do this after every "git checkout"
./gradlew build
```

The output will be in "build/".

## Running the examples

1. Follow the instructions in the "Build from source" section above.
2. Save your Dropbox API key in a file called "test.app".  See: [Get a Dropbox API key](#get-a-dropbox-api-key), above.
3. Compile with `./gradlew :sample:assembleRelease`

## Running the integration tests

1. Run through the `authorize` example above to get a "test.auth" file.
2. `./gradlew -Pcom.dropbox.test.authInfoFile=<path-to-test.auth> integrationTest`

To run individual tests, use the `--tests` gradle test filter:
```
./gradlew -Pcom.dropbox.test.authInfoFile=<path-to-test.auth> integrationTest --tests '*.DbxClientV1IT.testAccountInfo'
```

## FAQ

### Does this SDK require any special ProGuard rules for shrink optimizations?

Versions 2.0.0-2.0.3 of this SDK require SDK-specific ProGuard rules when shrinking is enabled. However, since version **2.0.4**, the only ProGuard rules necessary are for the SDK's required and optional dependencies. If you encounter ProGuard warnings, consider adding the following "-dontwarn" directives to your ProGuard configuration file:

```
-dontwarn okio.**
-dontwarn okhttp3.**
-dontwarn com.squareup.okhttp.**
-dontwarn com.google.appengine.**
-dontwarn javax.servlet.**
```

**IMPORTANT: If you are running version 2.0.x before 2.0.3, you should update to the latest Dropbox SDK version to avoid a deserialization bug that can cause Android apps that use ProGuard to crash.**
