plugins {
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.vanniktech.publish).apply(false)
    alias(libs.plugins.dokka)
}

allprojects {
    group = "io.github.yuroyami"
    version = "0.1.0"
}

// Aggregate the published library modules into a single Dokka API reference.
dependencies {
    dokka(project(":kite3d"))
}

dokka {
    moduleName.set("Kite3D")
}
