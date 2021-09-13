import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

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
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    kotlin("plugin.serialization")
    kotlin("plugin.parcelize")
    id("com.squareup.sqldelight")
    id("maven-publish")
}

kotlin {
    android { publishLibraryVariants("debug", "release") }

    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget =
        if (System.getenv("SDK_NAME")?.startsWith("iphoneos") == true)
            ::iosArm64
        else
            ::iosX64

    iosTarget("ios") {}

    cocoapods {
        summary = "Kotlin native mobile shared library"
        ios.deploymentTarget = "13.0"
        frameworkName = "shared"
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
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
                implementation(kotlin("test-junit"))
                implementation("junit:junit:${Version.junit}")
            }
        }
        val iosMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Version.kotlinCoroutines}") {
                    version {
                        strictly(Version.kotlinCoroutines)
                    }
                }
                // ktor
                implementation("io.ktor:ktor-client-ios:${Version.ktor}")
                // sqldelight
                implementation("com.squareup.sqldelight:native-driver:${Version.sqlDelight}")
            }
        }
    }

    // https://kotlinlang.org/docs/whatsnew1520.html#opt-in-export-of-kdoc-comments-to-generated-objective-c-headers
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].kotlinOptions.freeCompilerArgs += "-Xexport-kdoc"
    }
}

android {
    compileSdk = Version.Android.compileSdkVersion
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = Version.Android.minSdkVersion
        targetSdk = Version.Android.targetSdkVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
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

tasks.findByName("check")?.finalizedBy(tasks.findByName("iosTestOnSim"))

// https://cashapp.github.io/sqldelight/gradle/
sqldelight {
    database("ThisDatabase") {
        packageName = "com.example.db"
    }
}
