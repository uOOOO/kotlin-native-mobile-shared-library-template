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

group = property("group") as String
version = property("version") as String

kotlin {
    val artifactId: String by project
    val buildType = (project.findProperty("buildType") as? String ?: "debug").let {
        if ("debug" != it && "release" != it) {
            throw GradleException("Invalid buildType \"$it\". Please select debug or release.")
        }
        it
    }

    android("android") {
        publishLibraryVariants(buildType)
    }
    // Create and configure the targets.
    val iosX64 = iosX64("ios")
    val iosArm32 = iosArm32("ios32")
    val iosArm64 = iosArm64("ios64")

    configure(listOf(iosX64, iosArm32, iosArm64)) {
        binaries.framework {
            baseName = artifactId.capitalize()
        }
    }
    // Create a task building a fat framework.
    tasks.create("fatFramework", FatFrameworkTask::class) {
        // The fat framework must have the same base name as the initial frameworks.
        baseName = artifactId.capitalize()

        // The default destination directory is '<build directory>/fat-framework'.
        destinationDir = file("$buildDir/fat-framework/$buildType")

        // Specify the frameworks to be merged.
        from(
            iosX64.binaries.getFramework(buildType),
            iosArm32.binaries.getFramework(buildType),
            iosArm64.binaries.getFramework(buildType)
        )
    }
    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(kotlin("stdlib-common"))
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
            }
        }
        getByName("androidTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
        getByName("iosMain") {
            getByName("ios32Main").dependsOn(this)
            getByName("ios64Main").dependsOn(this)
        }
        getByName("iosTest") {
            getByName("ios32Test").dependsOn(this)
            getByName("ios64Test").dependsOn(this)
        }
    }

    afterEvaluate {
        tasks.findByName("test")?.finalizedBy(tasks.findByName("iosTest"))

        publishing.publications.all {
            (this as MavenPublication).apply {
                this.artifactId = artifactId.toLowerCase()
                if (name != "kotlinMultiplatform") {
                    this.artifactId =
                        "${artifactId.toLowerCase()}-${name.replace(Regex("${buildType.capitalize()}$$"), "")}"
                }
                this.version = version + if (buildType == "debug") "-SNAPSHOT" else ""
            }
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