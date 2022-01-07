package com.bennyhuo.kotlin.compiletesting.extensions.module

import com.bennyhuo.kotlin.compiletesting.extensions.source.SourceModuleInfo
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.*
import java.io.File
import javax.annotation.processing.AbstractProcessor

/**
 * Created by benny.
 */
abstract class KotlinModule(
    val name: String,
    val args: Map<String, String>,
    val sourceFiles: List<SourceFile>,
    val dependencyNames: List<String>
) {

    companion object {
        fun create(sourceModuleInfo: SourceModuleInfo, vararg kspProcessorProviders: SymbolProcessorProvider): KotlinModule {
            return KspKotlinModule(
                sourceModuleInfo.name,
                sourceModuleInfo.args,
                sourceModuleInfo.sourceFileInfos.map { sourceFileInfo ->
                    SourceFile.new(sourceFileInfo.fileName, sourceFileInfo.sourceBuilder.toString())
                },
                sourceModuleInfo.dependencies,
                *kspProcessorProviders
            )
        }

        fun create(sourceModuleInfo: SourceModuleInfo, vararg kaptProcessors: AbstractProcessor): KotlinModule {
            return KaptKotlinModule(
                sourceModuleInfo.name,
                sourceModuleInfo.args,
                sourceModuleInfo.sourceFileInfos.map { sourceFileInfo ->
                    SourceFile.new(sourceFileInfo.fileName, sourceFileInfo.sourceBuilder.toString())
                },
                sourceModuleInfo.dependencies,
                *kaptProcessors
            )
        }
    }

    val classpaths = ArrayList<File>()

    val dependencies = ArrayList<KotlinModule>()

    var isCompiled = false

    var compileResult: KotlinCompilation.Result? = null

    abstract val generatedSourceDir: File

    abstract val classesDir: File

    protected abstract fun setupArgs()

    protected val compilation = newCompilation().also {
        it.sources = sourceFiles
    }

    protected fun newCompilation(): KotlinCompilation {
        return KotlinCompilation().also { compilation ->
            compilation.inheritClassPath = true
            compilation.classpaths = classpaths
        }
    }

    private fun dependsOn(libraryUnit: KotlinModule) {
        classpaths += libraryUnit.classesDir
        classpaths += libraryUnit.classpaths

        dependencies += libraryUnit
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

    open fun compile() {
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

    override fun toString() =
        "$name: $isCompiled >> ${compileResult?.exitCode} ${compileResult?.messages}"

}