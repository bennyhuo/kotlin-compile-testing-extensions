package com.bennyhuo.kotlin.compiletesting.extensions.module

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspArgs
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import java.io.File

class KspKotlinModule(
    moduleName: String,
    args: Map<String, String>,
    sourceFiles: List<SourceFile>,
    dependencyNames: List<String>,
    vararg kspProcessorProviders: SymbolProcessorProvider
) : KotlinModule(moduleName, args, sourceFiles, dependencyNames) {

    init {
        compilation.symbolProcessorProviders += kspProcessorProviders
    }

    override val generatedSourceDir: File = compilation.kspSourcesDir

    private val realCompilation = newCompilation()
    override val classesDir: File = realCompilation.classesDir

    override fun setupArgs() {
        compilation.kspArgs.putAll(args)
        realCompilation.kspArgs.putAll(args)
    }

    override fun compile() {
        super.compile()

        realCompilation.sources = compilation.sources + compilation.kspSourcesDir.walkTopDown()
            .filter { !it.isDirectory }
            .map {
                SourceFile.new(it.name, it.readText())
            }

        compileResult = realCompilation.compile()
    }
}