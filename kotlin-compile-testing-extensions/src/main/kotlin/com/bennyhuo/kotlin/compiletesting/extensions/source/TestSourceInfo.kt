package com.bennyhuo.kotlin.compiletesting.extensions.source

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import javax.annotation.processing.Processor

class SourceModuleInfo(
    val name: String,
    val annotationProcessors: MutableList<String> = ArrayList(),
    val kaptArgs: MutableMap<String, String> = HashMap(),
    val symbolProcessorProviders: MutableList<String> = ArrayList(),
    val kspArgs: MutableMap<String, String> = HashMap(),
    val componentRegistrars: MutableList<String> = ArrayList(),
    val dependencies: MutableList<String> = ArrayList(),
    val sourceFileInfos: MutableList<SourceFileInfo> = ArrayList(),
    val entries: MutableList<Entry> = ArrayList()
) {
    @Suppress("UNCHECKED_CAST")
    private fun <T> MutableList<String>.instantiate(): List<T> {
        return map { SourceModuleInfo::class.java.classLoader.loadClass(it).constructors[0].newInstance() as T }
    }

    internal fun annotationProcessors() = annotationProcessors.instantiate<Processor>()

    internal fun symbolProcessorProviders() = symbolProcessorProviders.instantiate<SymbolProcessorProvider>()

    internal fun componentRegistrars() = componentRegistrars.instantiate<ComponentRegistrar>()

}

class ExpectModuleInfo(
    val name: String,
    val sourceFileInfos: MutableList<SourceFileInfo> = ArrayList()
)

class SourceFileInfo(val fileName: String) {
    internal val sourceBuilder = StringBuilder()

    val content: String
        get() = sourceBuilder.toString().trimEnd()

    override fun toString(): String {
        return "$fileName: \n${sourceBuilder}"
    }
}

/**
 * Function should be 'public static' and returns Java type 'void' without any parameters.
 */
class Entry(
    val className: String,
    val functionName: String
) {
    override fun toString(): String {
        return "$className.$functionName"
    }
}