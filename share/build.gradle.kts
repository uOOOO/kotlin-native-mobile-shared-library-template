import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

plugins {
    id("com.android.library")
    id("kotlin-multiplatform")
    id("kotlin-parcelize")
    id("kotlinx-serialization")
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
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    packagingOptions {
        exclude("META-INF/ktor-http.kotlin_module")
    }
}

kotlin {
    android {
        publishLibraryVariants("debug", "release")
    }
    ios {
        binaries.framework {
            baseName = frameworkId
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Version.kotlinSerialization}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.kotlinCoroutines}")
                // ktor
                implementation("io.ktor:ktor-client-core:${Version.ktor}")
                implementation("io.ktor:ktor-client-serialization:${Version.ktor}")
                implementation("io.ktor:ktor-client-logging:${Version.ktor}")
                // koin
                implementation("io.insert-koin:koin-core:${Version.koin}")
                // sqldelight
                implementation("com.squareup.sqldelight:coroutines-extensions:${Version.sqlDelight}")
                // reaktive
                api("com.badoo.reaktive:reaktive:${Version.reaktive}")
                api("com.badoo.reaktive:reaktive-annotations:${Version.reaktive}")
                api("com.badoo.reaktive:coroutines-interop:${Version.reaktive}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                // koin
                implementation("io.insert-koin:koin-test:${Version.koin}")
                // reaktive
                implementation("com.badoo.reaktive:reaktive-testing:${Version.reaktive}")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                // android
                implementation("androidx.appcompat:appcompat:${Version.appcompat}")
                implementation("androidx.core:core-ktx:${Version.coreKtx}")
                // ktor
                implementation("io.ktor:ktor-client-android:${Version.ktor}")
                // sqldelight
                implementation("com.squareup.sqldelight:android-driver:${Version.sqlDelight}")
                implementation("com.squareup.sqldelight:android-paging-extensions:${Version.sqlDelight}")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
        val iosMain by getting {
            dependencies {
                // ktor
                implementation("io.ktor:ktor-client-ios:${Version.ktor}")
                // sqldelight
                implementation("com.squareup.sqldelight:native-driver:${Version.sqlDelight}")
            }
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

//tasks.findByName("iosTest")?.finalizedBy(tasks.findByName("iosTestOnSim"))

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

afterEvaluate {
    publishing.publications
//        .filter { it.name != "kotlinMultiplatform" }
        .map { it as MavenPublication }
        .forEach { it.artifactId = it.artifactId.replace(project.name, artifactId).toLowerCase() }
    tasks.getByName("allTests").dependsOn(tasks.getByName("testReleaseUnitTest"))
}

tasks.withType(KotlinCompile::class).all {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString() // "1.8"
        useIR = true
    }
}

// https://cashapp.github.io/sqldelight/gradle/
sqldelight {
    database("ThisDatabase") {
        packageName = "com.example.db"
    }
}
