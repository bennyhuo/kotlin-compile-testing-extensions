package com.bennyhuo.kotlin.compiletesting.extensions.module

import com.bennyhuo.kotlin.compiletesting.extensions.compilation.runJvm
import com.bennyhuo.kotlin.compiletesting.extensions.ir.IrSourcePrinterRegistrar
import com.bennyhuo.kotlin.compiletesting.extensions.source.Entry
import com.bennyhuo.kotlin.compiletesting.extensions.source.SourceModuleInfo
import com.bennyhuo.kotlin.compiletesting.extensions.utils.readTextAndUnify
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspArgs
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import java.io.File
import java.net.URLClassLoader
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor

class KotlinModule(
    val name: String,
    val sourceFiles: List<SourceFile>,
    val entries: List<Entry>,
    val dependencyNames: List<String>,
    componentRegistrars: Collection<ComponentRegistrar> = emptyList(),
    kaptProcessors: Collection<Processor> = emptyList(),
    kaptArgs: Map<String, String> = emptyMap(),
    kspProcessorProviders: Collection<SymbolProcessorProvider> = emptyList(),
    kspArgs: Map<String, String> = emptyMap()
) {
    constructor(
        sourceModuleInfo: SourceModuleInfo,
        componentRegistrars: Collection<ComponentRegistrar> = emptyList(),
        kaptProcessors: Collection<AbstractProcessor> = emptyList(),
        kspProcessorProviders: Collection<SymbolProcessorProvider> = emptyList()
    ) : this(
        sourceModuleInfo.name,
        sourceModuleInfo.sourceFileInfos.map { sourceFileInfo ->
            SourceFile.new(sourceFileInfo.fileName, sourceFileInfo.sourceBuilder.toString())
        },
        sourceModuleInfo.entries,
        sourceModuleInfo.dependencies,
        componentRegistrars + sourceModuleInfo.componentRegistrars(),
        kaptProcessors + sourceModuleInfo.annotationProcessors(),
        sourceModuleInfo.kaptArgs,
        kspProcessorProviders + sourceModuleInfo.symbolProcessorProviders(),
        sourceModuleInfo.kspArgs,
    )

    private val classpath = ArrayList<File>()

    private val compilation = newCompilation()

    private val kspCompilation = if (kspProcessorProviders.isNotEmpty()) {
        newCompilation {
            symbolProcessorProviders = kspProcessorProviders.distinctBy { it.javaClass }.toList()
            this.kspArgs.putAll(kspArgs)
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

    val irTransformedSourceDir: File = compilation.workingDir.resolve("ir")

    internal val sourcePrinter = IrSourcePrinterRegistrar(irTransformedSourceDir)

    init {
        if (componentRegistrars.isNotEmpty()) {
            compilation.compilerPlugins += componentRegistrars.distinctBy { it.javaClass } + sourcePrinter
        }

        compilation.annotationProcessors += kaptProcessors.distinctBy { it.javaClass }
        compilation.kaptArgs.putAll(kaptArgs)
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
                    SourceFile.new(it.name, it.readTextAndUnify())
                }
        }

        compileResult = compilation.compile()
    }

    fun runJvm(): Map<String, String> {
        if (!isCompiled) {
            compile()
        }

        val compileResult = this.compileResult
        if (compileResult?.exitCode != KotlinCompilation.ExitCode.OK) {
            return emptyMap()
        }

        if (entries.isEmpty()) {
            return emptyMap()
        }

        val classLoader = URLClassLoader(
            (classpath + classesDir).map { it.toURI().toURL() }.toTypedArray(),
            this.javaClass.classLoader
        )

        return entries.associate {
            "$it.stdout" to compileResult.runJvm(it, classLoader)
        }
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

    private fun newCompilation(block: KotlinCompilation.() -> Unit = {}) =
        KotlinCompilation().also { compilation ->
            compilation.verbose = false
            compilation.inheritClassPath = true
            compilation.classpaths = classpath
            compilation.sources = sourceFiles
            compilation.moduleName = name

            compilation.block()
        }

    override fun toString() =
        "$name: $isCompiled >> ${compileResult?.exitCode} ${compileResult?.messages}"
}