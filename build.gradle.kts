plugins {
    // Plugins de Android y Kotlin
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.google.ksp) apply false
    // El plugin de Sonar
    alias(libs.plugins.sonarcloud)
    // El plugin de firebase
    id("com.google.gms.google-services") version "4.4.4" apply false
    alias(libs.plugins.android.library) apply false
    //Plugin de hilt
    id("com.google.dagger.hilt.android") version "2.54" apply false

}

apply(plugin = "org.sonarqube")

sonar {
    properties {
        property("sonar.projectKey", "TFG-Aplication_TFG")
        property("sonar.organization", "tfg-aplication")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.gradle.skipCompile", "true")

        // Tests
        property("sonar.tests", "core/src/test/java")
        property(
            "sonar.junit.reportPaths",
            "core/build/reports/tests/testDebugUnitTest/xml"
        )

        // Coverage
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "core/build/reports/jacoco/jacocoTestReport/xml/jacocoTestReport.xml"
        )

        // Binarios para análisis
        property(
            "sonar.kotlin.binaries",
            "core/build/tmp/kotlin-classes/debug"
        )

        // Exclusiones (generados, DI, etc.)
        property(
            "sonar.coverage.exclusions",
            "**/di/**,**/BuildConfig.*,**/*_Impl.*,**/Hilt_*.*,**/*Module_*.*"
        )
    }
}