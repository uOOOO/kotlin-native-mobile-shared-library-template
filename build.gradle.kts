// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven(url = "https://dl.bintray.com/kotlin/kotlin-dev")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${Version.agp}")
        classpath("org.jetbrains.kotlin.multiplatform:org.jetbrains.kotlin.multiplatform.gradle.plugin:${Version.kotlin}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Version.kotlin}")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${Version.kotlin}")
        classpath("com.squareup.sqldelight:gradle-plugin:${Version.sqlDelight}")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven(url = "https://dl.bintray.com/badoo/maven")
    }
}

tasks.register("clean").configure {
    delete(rootProject.buildDir)
}
