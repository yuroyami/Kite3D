enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// NOTE: the root project name must differ from every included module name.
// TYPESAFE_PROJECT_ACCESSORS derives an accessor from each, and a root named
// "kite3d" alongside `include(":kite3d")` generates `getKite3d()` twice, which
// fails configuration with "method getKite3d() is already defined".
rootProject.name = "kite3d-root"
include(":kite3d")
