import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    google()
    jcenter()
    mavenCentral()
    maven(url = "https://dl.bintray.com/badoo/maven")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    packagingOptions {
        exclude("META-INF/ktor-http.kotlin_module")
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
        publishLibraryVariants("debug", "release")
    }
    configure(listOf(iosX64("ios"), iosArm64())) {
        binaries.framework {
            baseName = frameworkId
        }
    }
    sourceSets {
        getByName("commonMain").dependencies {
            implementation(kotlin("stdlib-common"))
            // ktor
            implementation("io.ktor:ktor-client-core:${Version.ktor}")
            // kodein
            implementation("org.kodein.di:kodein-di-erased:${Version.kodein}")
            // sqldelight
            implementation("com.squareup.sqldelight:runtime:${Version.sqlDelight}")
            implementation("com.squareup.sqldelight:coroutines-extensions:${Version.sqlDelight}")
            // reaktive
            implementation("com.badoo.reaktive:reaktive:${Version.reaktive}")
            implementation("com.badoo.reaktive:coroutines-interop:${Version.reaktive}")
        }
        getByName("commonTest").dependencies {
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
            // reaktive
            implementation("com.badoo.reaktive:reaktive-testing:${Version.reaktive}")
        }
        getByName("androidMain").dependencies {
            implementation(kotlin("stdlib"))
            // android
            implementation("androidx.appcompat:appcompat:${Version.appcompat}")
            implementation("androidx.core:core-ktx:${Version.coreKtx}")
            // ktor
            implementation("io.ktor:ktor-client-android:${Version.ktor}")
            // kodein
            implementation("org.kodein.di:kodein-di-conf:${Version.kodein}")
            // sqldelight
            implementation("com.squareup.sqldelight:android-driver:${Version.sqlDelight}")
            implementation("com.squareup.sqldelight:android-paging-extensions:${Version.sqlDelight}")
        }
        getByName("androidTest").dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-junit"))
        }
        getByName("iosMain") {
            dependencies {
                // ktor
                implementation("io.ktor:ktor-client-ios:${Version.ktor}")
                // sqldelight
                implementation("com.squareup.sqldelight:native-driver:${Version.sqlDelight}")
            }
            getByName("iosArm64Main").dependsOn(this)
        }
    }
}

tasks.register("iosTestOnSim") {
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

tasks.findByName("iosTest")?.finalizedBy(tasks.findByName("iosTestOnSim"))

afterEvaluate {
    publishing.publications
//        .filter { it.name != "kotlinMultiplatform" }
        .map { it as MavenPublication }
        .forEach { it.artifactId = it.artifactId.replace(project.name, artifactId).toLowerCase() }
}

tasks.withType(KotlinCompile::class).all {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString() // "1.8"
    }
}

// https://cashapp.github.io/sqldelight/gradle/
sqldelight {
    database("ThisDatabase") {
        packageName = "${group}.db"
        sourceFolders = listOf("sqldelight")
        schemaOutputDirectory = file("$buildDir/sqldelight")
    }
}
