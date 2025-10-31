import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("org.apache.commons:commons-math3:3.6.1")
            implementation("org.scilab.forge:jlatexmath:1.0.7")
            implementation("org.jetbrains.lets-plot:lets-plot-compose:3.0.0")
            implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.7.3")
            implementation("org.jetbrains.lets-plot:lets-plot-batik:4.7.3")
        }
    }
}


compose.desktop {
    application {
        mainClass = "org.lab_03.project.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.lab_03.project"
            packageVersion = "1.0.0"
        }
    }
}
