import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish")
}

group = property("GROUP").toString()
version = property("VERSION_NAME").toString()

dependencies {
    api("dev.zacsweers.kctfork:core:0.13.0")
    api("dev.zacsweers.kctfork:ksp:0.13.0")

    api("com.google.devtools.ksp:symbol-processing-api:2.3.9")
    
    api(project(":kotlin-source-printer"))

    implementation(kotlin("test-common"))
    implementation(kotlin("test-annotations-common"))
    implementation(kotlin("test-junit"))
}

kotlin {
    jvmToolchain(8)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xmulti-dollar-interpolation"
        )
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}