plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish")
}

dependencies {
    api("dev.zacsweers.kctfork:core:0.7.0")
    api("dev.zacsweers.kctfork:ksp:0.7.0")

    api("com.google.devtools.ksp:symbol-processing-api:2.1.0-1.0.29")

    implementation(kotlin("test-common"))
    implementation(kotlin("test-annotations-common"))
    implementation(kotlin("test-junit"))
}

kotlin {
    jvmToolchain(8)
}
