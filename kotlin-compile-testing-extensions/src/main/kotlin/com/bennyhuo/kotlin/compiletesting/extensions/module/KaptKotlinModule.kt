package com.bennyhuo.kotlin.compiletesting.extensions.module

import com.tschuchort.compiletesting.SourceFile
import java.io.File
import javax.annotation.processing.AbstractProcessor

class KaptKotlinModule(
    moduleName: String,
    args: Map<String, String>,
    sourceFiles: List<SourceFile>,
    dependencyNames: List<String>,
    vararg kaptProcessors: AbstractProcessor,
) : KotlinModule(moduleName, args, sourceFiles, dependencyNames) {

    init {
        compilation.annotationProcessors += kaptProcessors
    }

    override val generatedSourceDir: File = compilation.kaptSourceDir
    override val classesDir: File = compilation.classesDir

    override fun setupArgs() {
        compilation.kaptArgs.putAll(args)
    }
}
