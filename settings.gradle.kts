rootProject.name = "Kotlin_Native_Mobile_Shared_Library_Template"
enableFeaturePreview("GRADLE_METADATA")
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.library") {
                useModule("com.android.tools.build:gradle:3.5.3")
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