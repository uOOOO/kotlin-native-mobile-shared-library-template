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
./gradlew -PbuildType=${buildType} fatFramework

ex) if buildType is release,
./gradlew -PbuildType=release fatFramework

The result is in ${ROOT_PROJECT_DIR}/build/bin/fat-framework/${buildType}
```

And you can set group, artifactId and version manually. They are in gradle.properties.

## TODO
- Upload artifact to repository
- And more...
