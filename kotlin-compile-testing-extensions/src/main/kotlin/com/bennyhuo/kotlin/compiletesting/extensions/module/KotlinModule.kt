package com.bennyhuo.kotlin.compiletesting.extensions.module

import com.bennyhuo.kotlin.compiletesting.extensions.source.SourceModuleInfo
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspArgs
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import java.io.File
import javax.annotation.processing.AbstractProcessor

class KotlinModule(
    val name: String,
    args: Map<String, String>,
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

    private val classpath = ArrayList<File>()

    private val compilation = KotlinCompilation().also { compilation ->
        compilation.inheritClassPath = true
        compilation.classpaths = classpath
        compilation.sources = sourceFiles
        compilation.moduleName = name
    }

    private val kspCompilation = if (kspProcessorProviders.isNotEmpty()) {
        KotlinCompilation().also { compilation ->
            compilation.inheritClassPath = true
            compilation.classpaths = classpath
            compilation.sources = sourceFiles
            compilation.moduleName = name
            compilation.symbolProcessorProviders += kspProcessorProviders
            compilation.kspArgs.putAll(args)
        }
    } else {
        null
    }

    private val classesDir: File = compilation.classesDir

    val dependencies = ArrayList<KotlinModule>()

    var isCompiled = false
        private set

    var compileResult: KotlinCompilation.Result? = null

    val generatedSourceDirs: List<File> = listOfNotNull(
        compilation.kaptSourceDir,
        compilation.kaptKotlinGeneratedDir,
        kspCompilation?.kspSourcesDir
    )

    init {
        compilation.compilerPlugins += componentRegistrars

        compilation.annotationProcessors += kaptProcessors
        compilation.kaptArgs.putAll(args)

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

        if (kspCompilation != null) {
            val compileResult = kspCompilation.compile()
            if (compileResult.exitCode != KotlinCompilation.ExitCode.OK) {
                this.compileResult = compileResult
                return
            }

            compilation.sources += kspCompilation.kspSourcesDir.walkTopDown()
                .filter { !it.isDirectory }
                .map {
                    SourceFile.new(it.name, it.readText())
                }
        }

        compileResult = compilation.compile()
    }

    private fun ensureDependencies() {
        dependencies.forEach {
            it.compile()
            classpath += it.classesDir
            classpath += it.classpath
        }
    }

    private fun dependsOn(module: KotlinModule) {
        dependencies += module
    }

    override fun toString() =
        "$name: $isCompiled >> ${compileResult?.exitCode} ${compileResult?.messages}"
}