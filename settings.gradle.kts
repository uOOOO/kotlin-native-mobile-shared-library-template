rootProject.name = "Kotlin_Native_Mobile_Shared_Library_Template"
enableFeaturePreview("GRADLE_METADATA")
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.library") {
                useModule("com.android.tools.build:gradle:${Version.agp}")
            } else if (requested.id.id == "com.squareup.sqldelight") {
                useModule("com.squareup.sqldelight:gradle-plugin:${Version.sqlDelight}")
            }
        }
    }
    repositories {
        gradlePluginPortal()
        google()
        jcenter()
        mavenCentral()
    }
}