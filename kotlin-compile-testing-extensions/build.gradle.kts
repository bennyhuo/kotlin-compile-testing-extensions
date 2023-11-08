plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish")
}

dependencies {
    api("dev.zacsweers.kctfork:core:0.4.0")
    api("dev.zacsweers.kctfork:ksp:0.4.0")

    api("com.google.devtools.ksp:symbol-processing-api:1.9.20-1.0.14")

    implementation(kotlin("test-common"))
    implementation(kotlin("test-annotations-common"))
    implementation(kotlin("test-junit"))
}

kotlin {
    jvmToolchain(8)
}
