plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish")
}

dependencies {
    api("dev.zacsweers.kctfork:core:0.5.0")
    api("dev.zacsweers.kctfork:ksp:0.5.0")

    api("com.google.devtools.ksp:symbol-processing-api:2.0.0-1.0.22")

    implementation(kotlin("test-common"))
    implementation(kotlin("test-annotations-common"))
    implementation(kotlin("test-junit"))
}

kotlin {
    jvmToolchain(8)
}
