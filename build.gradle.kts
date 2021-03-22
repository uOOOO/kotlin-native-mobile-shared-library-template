// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${Version.agp}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Version.kotlin}")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${Version.kotlin}")
        classpath("com.squareup.sqldelight:gradle-plugin:${Version.sqlDelight}")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}

tasks.register("clean").configure {
    delete(rootProject.buildDir)
}
