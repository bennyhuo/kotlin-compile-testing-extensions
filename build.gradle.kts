// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
    }
    dependencies {
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.18.0")
    }
}

plugins {
    kotlin("jvm") version "1.7.10" apply false
    id("org.jetbrains.dokka") version "1.7.10" apply false
}

subprojects {
    repositories {
        maven("https://mirrors.tencent.com/nexus/repository/maven-public/")
    }
}
