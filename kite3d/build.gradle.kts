import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.vanniktech.publish)
    alias(libs.plugins.dokka)
}

/*
 * :kite3d is the pure-Kotlin core — a from-scratch port of three.js (MIT) into
 * common Kotlin: math, scene graph, geometry, animation and the TSL shader
 * graph. NO external runtime deps; only kotlin-stdlib is on the classpath.
 * Tests use kotlin-test.
 *
 * The core is 100% common Kotlin (no expect/actual, no cinterop, no JNI), so it
 * compiles for every target Kotlin supports. GPU submission lives in separate
 * backend modules (e.g. :kite3d-backend-wgpu) added later — they are the only
 * place platform/native code appears.
 *
 * NOTE: the Android target (com.android.kotlin.multiplatform.library) is
 * intentionally omitted for now — it requires a configured Android SDK. The
 * code is target-agnostic, so adding android later is purely a build-file change.
 */
kotlin {
    jvmToolchain(21)

    jvm()

    // Apple
    iosSimulatorArm64()
    iosArm64()
    iosX64()
    macosArm64()
    macosX64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()
    watchosDeviceArm64()

    // Native desktop / server
    linuxX64()
    linuxArm64()
    mingwX64()

    // Android NDK
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()

    // Web
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    js(IR) {
        browser()
        nodejs()
        binaries.library()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
    }

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
                optIn("kotlin.experimental.ExperimentalNativeApi")
            }
        }

        commonMain.dependencies {
            // Intentionally empty. Kite3D core depends on kotlin-stdlib only.
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
