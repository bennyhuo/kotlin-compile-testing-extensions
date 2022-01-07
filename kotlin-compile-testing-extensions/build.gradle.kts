plugins {
    kotlin("jvm")

    id("com.vanniktech.maven.publish")
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:1.6.0-1.0.1")

    api("com.github.tschuchortdev:kotlin-compile-testing:1.4.6")
    api("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.6")

    implementation(kotlin("test-common"))
    implementation(kotlin("test-annotations-common"))
    implementation(kotlin("test-junit"))
}
