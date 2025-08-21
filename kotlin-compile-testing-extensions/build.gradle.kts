import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish")
}

group = property("GROUP").toString()
version = property("VERSION_NAME").toString()

dependencies {
    api("dev.zacsweers.kctfork:core:0.8.0")
    api("dev.zacsweers.kctfork:ksp:0.8.0")

    api("com.google.devtools.ksp:symbol-processing-api:2.2.0-2.0.2")

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