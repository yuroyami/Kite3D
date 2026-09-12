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

// Shared Kite theme. The source is in ../_kite-docs, and its sync.sh copies it here,
// so this repo builds on its own from a fresh clone.
//
// Apply it to every project with Dokka, not only the root. The root renders only the
// "all modules" page; each module renders its own API pages with its own settings.
allprojects {
    plugins.withId("org.jetbrains.dokka") {
        extensions.configure<org.jetbrains.dokka.gradle.DokkaExtension> {
            pluginsConfiguration.html {
                customStyleSheets.from(
                    rootProject.layout.projectDirectory.file("docs/api-theme/kite.css"),
                )
                templatesDir.set(
                    rootProject.layout.projectDirectory.dir("dokka-templates"),
                )
                footerMessage.set("MIT · Kite3D is part of the Kite family.")
            }

            // A module with a Module.md gets its description onto the aggregated
            // "all modules" landing page, which is otherwise a bare list of names.
            dokkaSourceSets.configureEach {
                val moduleDoc = layout.projectDirectory.file("Module.md")
                if (moduleDoc.asFile.exists()) {
                    includes.from(moduleDoc)
                }
            }

        }
    }
}

