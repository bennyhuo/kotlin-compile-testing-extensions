package com.bennyhuo.kotlin.compiletesting.extensions.module

import com.bennyhuo.kotlin.compiletesting.extensions.source.SourceModuleInfo
import com.bennyhuo.kotlin.compiletesting.ksp.kspArgs
import com.bennyhuo.kotlin.compiletesting.ksp.kspSourcesDir
import com.bennyhuo.kotlin.compiletesting.ksp.kspWithCompilation
import com.bennyhuo.kotlin.compiletesting.ksp.symbolProcessorProviders
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import java.io.File
import javax.annotation.processing.AbstractProcessor

class KotlinModule(
    val name: String,
    val args: Map<String, String>,
    val sourceFiles: List<SourceFile>,
    val dependencyNames: List<String>,
    componentRegistrars: Collection<ComponentRegistrar> = emptyList(),
    kaptProcessors: Collection<AbstractProcessor> = emptyList(),
    kspProcessorProviders: Collection<SymbolProcessorProvider> = emptyList()
) {
    constructor(
        sourceModuleInfo: SourceModuleInfo,
        componentRegistrars: Collection<ComponentRegistrar> = emptyList(),
        kaptProcessors: Collection<AbstractProcessor> = emptyList(),
        kspProcessorProviders: Collection<SymbolProcessorProvider> = emptyList()
    ) : this(
        sourceModuleInfo.name,
        sourceModuleInfo.args,
        sourceModuleInfo.sourceFileInfos.map { sourceFileInfo ->
            SourceFile.new(sourceFileInfo.fileName, sourceFileInfo.sourceBuilder.toString())
        },
        sourceModuleInfo.dependencies,
        componentRegistrars, kaptProcessors, kspProcessorProviders
    )

    private val compilation = KotlinCompilation().also { compilation ->
        compilation.inheritClassPath = true
        compilation.classpaths = classpath
        compilation.sources = sourceFiles
        compilation.moduleName = name
    }

    private val classesDir: File = compilation.classesDir

    private val classpath = ArrayList<File>()

    val dependencies = ArrayList<KotlinModule>()

    var isCompiled = false
        private set

    var compileResult: KotlinCompilation.Result? = null

    val generatedSourceDirs: List<File> = listOf(compilation.kaptSourceDir, compilation.kspSourcesDir)

    init {
        compilation.compilerPlugins += componentRegistrars
        compilation.annotationProcessors += kaptProcessors
        compilation.symbolProcessorProviders += kspProcessorProviders
        compilation.kspWithCompilation = true
    }

    fun addSourceFiles(sourceFiles: List<SourceFile>) {
        compilation.sources += sourceFiles
    }

    fun resolveDependencies(kotlinModuleMap: Map<String, KotlinModule>) {
        dependencyNames.mapNotNull {
            kotlinModuleMap[it]
        }.forEach {
            dependsOn(it)
        }
    }

    fun compile() {
        if (isCompiled) return
        ensureDependencies()

        isCompiled = true

        setupArgs()
        compileResult = compilation.compile()
    }

    private fun ensureDependencies() {
        dependencies.forEach {
            it.compile()
        }
    }

    private fun dependsOn(libraryUnit: KotlinModule) {
        classpath += libraryUnit.classesDir
        classpath += libraryUnit.classpath

        dependencies += libraryUnit
    }

    private fun setupArgs() {
        compilation.kaptArgs.putAll(args)
        compilation.kspArgs.putAll(args)
    }

    override fun toString() =
        "$name: $isCompiled >> ${compileResult?.exitCode} ${compileResult?.messages}"
}