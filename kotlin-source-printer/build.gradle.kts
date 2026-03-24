import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish")
}

group = property("GROUP").toString()
version = property("VERSION_NAME").toString()

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable")
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