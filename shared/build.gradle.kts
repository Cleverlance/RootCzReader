plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.6.20"
    id("com.android.library")
}

kotlin {
    android()
    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        val ktorVersion = "2.0.0"
        val xmlUtilVersion = "0.84.2"

        val commonMain by getting {
            dependencies {
                dependencies {
                    implementation("io.ktor:ktor-client-core:$ktorVersion")
                    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                    implementation("io.github.pdvrieze.xmlutil:core:$xmlUtilVersion")
                    implementation("io.github.pdvrieze.xmlutil:serialization:$xmlUtilVersion")
                }
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            }
        }
    }
}

android {
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 31
    }
}