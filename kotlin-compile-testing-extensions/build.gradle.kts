plugins {
    kotlin("jvm")

    id("com.vanniktech.maven.publish")
}

dependencies {
    api("com.github.tschuchortdev:kotlin-compile-testing:1.4.9")
    api("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.9")

    api("com.google.devtools.ksp:symbol-processing-api:1.7.10-1.0.6")

    implementation(kotlin("test-common"))
    implementation(kotlin("test-annotations-common"))
    implementation(kotlin("test-junit"))
}
