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

// The root name must differ from every module name. Otherwise type-safe accessors
// create getKite3d() twice and fail with "method getKite3d() is already defined".
rootProject.name = "kite3d-root"
include(":kite3d")
