import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.vanniktech.publish)
    alias(libs.plugins.dokka)
}

/*
 * :kite3d is the published library — a file-for-file port of three.js r184's
 * src/math (MIT) into common Kotlin. It ships the math layer and nothing else:
 * io.github.yuroyami.kite3d.math plus its interpolants sub-package. There is no
 * scene graph, geometry, animation, shader graph or renderer here; those layers
 * are not written yet, and GPU submission would live in separate backend
 * modules that do not exist yet.
 *
 * NO external runtime deps; only kotlin-stdlib is on the classpath. Tests use
 * kotlin-test. The source is 100% common Kotlin (no expect/actual, no cinterop,
 * no JNI), so it compiles for every target Kotlin supports.
 *
 * NOTE: the Android target (com.android.kotlin.multiplatform.library) is
 * intentionally omitted for now — it requires a configured Android SDK. No
 * -android artifact is published, so Android consumers resolve the jvm variant
 * instead. The code is target-agnostic and the JVM bytecode target is 11, so
 * adding android later is purely a build-file change.
 */
kotlin {
    jvmToolchain(21)

    // Every unmarked declaration in a published library would otherwise leak as
    // public API. Strict explicit-API forces an intentional visibility on each.
    explicitApi()

    // Checked-in ABI dumps under kite3d/api/. `checkKotlinAbi` fails the build when
    // the public API drifts from the dump, so a breaking change can never land
    // silently; `updateKotlinAbi` regenerates it when the change is intended.
    // Calling the block is what enables validation; klib dumps (native/js/wasm)
    // are produced alongside the JVM one automatically.
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {}

    jvm {
        // Build with JDK 21 (toolchain) but emit bytecode that JDK 11 runtimes
        // and Android's D8 can consume. See audit §8.2 B2.
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
