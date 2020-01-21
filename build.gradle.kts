import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask

repositories {
    google()
    jcenter()
    mavenCentral()
}

plugins {
    kotlin("multiplatform") version Version.kotlin
    id("com.android.library")
    kotlin("android.extensions") version Version.kotlin
    id("maven-publish")
    id("com.squareup.sqldelight")
}

android {
    compileSdkVersion(Version.Android.compileSdkVersion)
    defaultConfig {
        minSdkVersion(Version.Android.minSdkVersion)
        targetSdkVersion(Version.Android.targetSdkVersion)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            java.srcDirs("src/androidMain/kotlin")
            res.srcDirs("src/androidMain/res")
        }
        getByName("androidTest") {
            java.srcDirs("src/androidTest/kotlin")
            res.srcDirs("src/androidTest/res")
        }
    }
}

group = property("groupId") as String
version = property("version") as String
val artifactId by lazy { property("artifactId") as String }
val frameworkId by lazy { property("frameworkId") as String }
val buildType by lazy {
    (project.findProperty("buildType") as? String ?: "debug").apply {
        if ("debug" != this && "release" != this) {
            throw GradleException("Invalid buildType \"$this\". Please select debug or release.")
        }
    }
}

kotlin {
    android {
        publishLibraryVariants(buildType)
    }
    configure(listOf(iosX64("ios"), iosArm32(), iosArm64())) {
        binaries.framework {
            baseName = frameworkId
        }
    }
    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("io.ktor:ktor-client-core:${Version.ktor}")
                implementation("org.kodein.di:kodein-di-erased:${Version.kodein}")
                implementation("com.squareup.sqldelight:runtime:${Version.sqlDelight}")
//                implementation("com.squareup.sqldelight:coroutines-extensions:${Version.sqlDelight}")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        getByName("androidMain") {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("androidx.appcompat:appcompat:${Version.appcompat}")
                implementation("androidx.core:core-ktx:${Version.coreKtx}")
                implementation("io.ktor:ktor-client-android:${Version.ktor}")
                implementation("com.squareup.sqldelight:android-driver:${Version.sqlDelight}")
//                implementation("com.squareup.sqldelight:coroutines-extensions-jvm:${Version.sqlDelight}")
            }
        }
        getByName("androidTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
        getByName("iosMain") {
            dependencies {
                implementation("io.ktor:ktor-client-ios:${Version.ktor}")
                implementation("com.squareup.sqldelight:ios-driver:${Version.sqlDelight}")
            }
            getByName("iosArm32Main").dependsOn(this)
            getByName("iosArm64Main").dependsOn(this)
        }
        getByName("iosTest") {
            getByName("iosArm32Test").dependsOn(this)
            getByName("iosArm64Test").dependsOn(this)
        }
    }
}

tasks.register("iosTest") {
    val device = project.findProperty("iosDevice") as? String ?: "iPhone 8"
    dependsOn("linkDebugTestIos")
    group = JavaBasePlugin.VERIFICATION_GROUP
    description = "Runs tests for target 'ios' on an iOS simulator"

    doLast {
        val binary = (kotlin.targets["ios"] as KotlinNativeTarget).binaries.getTest("DEBUG").outputFile
        try {
            exec {
                commandLine("xcrun", "simctl", "boot", device)
            }
            exec {
                commandLine("xcrun", "simctl", "spawn", "--standalone", device, binary.absolutePath)
            }
        } finally {
            exec {
                commandLine("xcrun", "simctl", "shutdown", device)
            }
        }
    }
}

// Create a task building a fat framework.
tasks.register("linkFatFrameworkIos", FatFrameworkTask::class) {
    // The fat framework must have the same base name as the initial frameworks.
    baseName = frameworkId

    // The default destination directory is '<build directory>/bin/iosFat'.
    destinationDir = file("$buildDir/bin/iosFat/${buildType}Framework")

    // Specify the frameworks to be merged.
    from(kotlin.targets.filter { it.name.startsWith("ios") }
        .map { it as KotlinNativeTarget }
        .map { it.binaries.getFramework(buildType) })
}

tasks.findByName("test")?.finalizedBy(tasks.findByName("iosTest"))

afterEvaluate {
    publishing.publications
        .filter { it.name != "kotlinMultiplatform" }
        .map { it as MavenPublication }
        .forEach {
            it.version += if (buildType == "debug") "-SNAPSHOT" else ""
            it.artifactId =
                "${artifactId.toLowerCase()}-${it.name.replace(Regex("${buildType.capitalize()}$$"), "")}"
        }
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

// https://cashapp.github.io/sqldelight/gradle/
sqldelight {
    database("ThisDatabase") {
        packageName = "${group}.db"
        sourceFolders = listOf("sqldelight")
        schemaOutputDirectory = file("$buildDir/sqldelight")
    }
}