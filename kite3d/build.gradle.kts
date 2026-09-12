import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.vanniktech.publish)
    alias(libs.plugins.dokka)
}

/*
 * :kite3d is the published library: three.js r184's src/math (MIT) in common Kotlin,
 * plus Kite3D's own Camera. Packages: io.github.yuroyami.kite3d.math and its
 * interpolants sub-package. No scene graph, geometry, animation or renderer.
 *
 * The only runtime dependency is kotlin-stdlib. Tests use kotlin-test. All source is
 * common Kotlin (no expect/actual, no cinterop, no JNI).
 *
 * There is no Android library target, because it needs an Android SDK to build.
 * Android apps get the jvm artifact, which targets JVM 11.
 */
kotlin {
    jvmToolchain(21)

    // Every unmarked declaration in a published library would otherwise leak as
    // public API. Strict explicit-API forces an intentional visibility on each.
    explicitApi()

    // ABI dumps live in kite3d/api/. `checkKotlinAbi` fails the build when the public API
    // differs from them, and `updateKotlinAbi` rewrites them. The empty block turns this on.
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {}

    jvm {
        // Build with JDK 21, but emit JDK 11 bytecode for JDK 11 runtimes and Android's D8.
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

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
    js {
        browser()
        nodejs()
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
        commonMain.dependencies {
            // Intentionally empty. Kite3D core depends on kotlin-stdlib only.
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
