plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.sonarcloud) // Añade esto aquí
}

// Configuración de Sonar
sonar {
    properties {
        property("sonar.projectKey", "TFG-Aplication_TFG") // Lo sacas de la web de Sonar
        property("sonar.organization", "tfg-aplication")
        property("sonar.host.url", "https://sonarcloud.io")

        property("sonar.kotlin.binaries", "**/build/classes/kotlin")
        property("sonar.coverage.jacoco.xmlReportPaths", "**/build/reports/jacoco/**/*.xml")
    }
}