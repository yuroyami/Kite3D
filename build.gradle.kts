plugins {
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.vanniktech.publish).apply(false)
    alias(libs.plugins.dokka)
}

// group and version are set once, for all projects, in the root gradle.properties.

// Aggregate the published library modules into a single Dokka API reference.
dependencies {
    dokka(projects.kite3d)
}

dokka {
    moduleName.set("Kite3D")
}
