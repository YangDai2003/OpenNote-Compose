// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.googleHilt) apply false
    alias(libs.plugins.googleKsp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.serialization) apply false
}

//subprojects {
//    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
//        kotlinOptions {
//            if (project.findProperty("composeCompilerReports") == "true") {
//                freeCompilerArgs += listOf(
//                    "-P",
//                    "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${project.buildDir.absolutePath}/compose_compiler"
//                )
//            }
//            if (project.findProperty("composeCompilerMetrics") == "true") {
//                freeCompilerArgs += listOf(
//                    "-P",
//                    "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${project.buildDir.absolutePath}/compose_compiler"
//                )
//            }
//        }
//    }
//}
