pluginManagement { 
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement { 
    repositories { 
        mavenCentral()
    }
}

include(":sample",":kotlin-compile-testing-extensions")