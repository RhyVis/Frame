import com.strumenta.antlrkotlin.gradle.AntlrKotlinTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.antlr)
    alias(libs.plugins.kotlinx.serialization)
}

group = "rhx.frame"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.log)
    implementation(libs.antlr.kotlin)
    implementation(libs.bundles.kotlinx)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
    sourceSets {
        main {
            kotlin.srcDir(layout.buildDirectory.dir("generated/antlr"))
        }
    }
}

allOpen {
    annotation("rhx.frame.annotation.OpenClass")
}

val generateKotlinGrammarSource =
    tasks.register<AntlrKotlinTask>("generateKotlinGrammarSource") {
        dependsOn("cleanGenerateKotlinGrammarSource")

        source =
            fileTree(layout.projectDirectory.dir("antlr")) {
                include("**/*.g4")
            }

        val pkgName = "rhx.frame.antlr"
        packageName = pkgName

        arguments = listOf("-visitor")

        // Generated files are outputted inside build/generatedAntlr/{package-name}
        val outDir = "generated/antlr/${pkgName.replace(".", "/")}"
        outputDirectory =
            layout.buildDirectory
                .dir(outDir)
                .get()
                .asFile
    }

tasks.withType<KotlinCompile> {
    dependsOn(generateKotlinGrammarSource)
}
