plugins {
    kotlin("jvm")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")

    api("org.slf4j:slf4j-api:1.7.25")
    api("ch.qos.logback:logback-core:1.3.0-alpha10")

    implementation(project(":kotlin-compile-testing-extensions"))
}
