plugins {
    // Plugins de Android y Kotlin
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false

    // El plugin de Sonar
    alias(libs.plugins.sonarcloud)
}

sonar {
    properties {
        property("sonar.projectKey", "TFG-Aplication_TFG")
        property("sonar.organization", "tfg-aplication")
        property("sonar.host.url", "https://sonarcloud.io")

        property("sonar.gradle.skipCompile", "true")

        property("sonar.kotlin.binaries", "**/build/classes/kotlin")

        property("sonar.coverage.jacoco.xmlReportPaths", "**/build/reports/jacoco/**/*.xml")
    }
}