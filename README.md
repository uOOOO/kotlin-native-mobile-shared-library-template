# kotlin-native-mobile-shared-library-template
This is a template for Kotlin native mobile shared library.
You can make Android AAR and iOS Framework with this project.

- Android AAR
```shell script
./gradlew -PbuildType=${buildType} publishAndroid${buildType}PublicationToMavenLocal

ex) if buildType is debug,
./gradlew -PbuildType=debug publishAndroidDebugPublicationToMavenLocal
```

- iOS Fat Framework (X64 + arm32 + arm64)
```shell script
./gradlew -PbuildType=${buildType} linkFatFrameworkIos

ex) if buildType is release,
./gradlew -PbuildType=release linkFatFrameworkIos

The result is in ${ROOT_PROJECT_DIR}/build/bin/iosFat/${buildType}
```

And you can set groupId, artifactId and version manually in ${ROOT_PROJECT_DIR}/gradle.properties.

## TODO
- Upload artifact to repository
- And more...
