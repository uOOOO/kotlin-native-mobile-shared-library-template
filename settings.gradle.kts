rootProject.name = "Kotlin_Native_Mobile_Shared_Library_Template"
enableFeaturePreview("GRADLE_METADATA")
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.library") {
                useModule("com.android.tools.build:gradle:${Version.agp}")
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