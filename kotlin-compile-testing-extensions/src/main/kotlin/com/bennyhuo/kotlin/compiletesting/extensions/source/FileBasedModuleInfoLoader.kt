package com.bennyhuo.kotlin.compiletesting.extensions.source

import java.io.File

/**
 * Created by benny at 2022/1/7 10:29 AM.
 */
private const val SOURCE_START_LINE = "// SOURCE"
private const val EXPECT_START_LINE = "// EXPECT"
private val FILE_NAME_PATTERN = Regex("""// FILE: (([$\w.]+)\.([$\w]+))\s*(\[(\S*)#(\S*)])?""")
private val MODULE_NAME_PATTERN =
    Regex("""// MODULE: ([-\w]+)(\s*/\s*(([-\w]+)(\s*,\s*([-\w]+))*))?\s*""")

private const val ARGS_PREFIX = "// ARGS:"
private const val KSP_PREFIX = "// KSP:"
private const val KSP_ARGS_PREFIX = "// KSP_ARGS:"
private const val KAPT_PREFIX = "// KAPT: "
private const val KAPT_ARGS_PREFIX = "// KAPT_ARGS:"
private const val KCP_LEGACY_PREFIX = "// KCP_LEGACY:"
private const val KCP_PREFIX = "// KCP:"
private const val ENTRY_PREFIX = "// ENTRY:"

private const val DEFAULT_MODULE = "default-module"
private const val DEFAULT_FILE = "DefaultFile.kt"

class FileBasedModuleInfoLoader(filePath: String) :
    TextBasedModuleInfoLoader(File(filePath).readText())

open class TextBasedModuleInfoLoader(private val text: String) : ModuleInfoLoader {

    private val lines by lazy {
        text.lines().dropWhile { it.trim() != SOURCE_START_LINE }
    }

    private val sourceLines by lazy {
        lines.takeWhile { it.trim() != EXPECT_START_LINE }.drop(1)
    }

    private val expectLines by lazy {
        lines.dropWhile { it.trim() != EXPECT_START_LINE }.drop(1)
    }

    override fun loadSourceModuleInfos(): Collection<SourceModuleInfo> {

        fun parseFileInfo(moduleInfo: SourceModuleInfo, input: ListIterator<String>) {
            while (input.hasNext()) {
                val line = input.next()
                if (line.startsWith(ENTRY_PREFIX)) {
                    val splits = line.removePrefix(ENTRY_PREFIX).trim().split("#")
                    check(splits.size == 2) {
                        "Both class name and function name is required for an entry."
                    }
                    val className = splits[0]
                    val functionParts = splits[1].split("(", limit = 2)
                    val functionName = functionParts[0]
                    val args = if (functionParts.size == 2) {
                        val argsPart =  functionParts[1]
                        check(argsPart.endsWith(")")) {
                            "Invalid function argument list."
                        }
                        argsPart.removeSuffix(")").split(",").map { it.trim() }
                    } else emptyList()
                    moduleInfo.entries += Entry(className, functionName, args)
                } else {
                    input.previous()
                    break
                }
            }
        }

        fun parseFiles(moduleInfo: SourceModuleInfo, input: ListIterator<String>) {
            while (input.hasNext()) {
                val line = input.next()
                if (line.startsWith("// MODULE:")) {
                    input.previous()
                    break
                }
                val result = FILE_NAME_PATTERN.find(line)
                if (result == null) {
                    if (moduleInfo.sourceFileInfos.isEmpty()) {
                        moduleInfo.sourceFileInfos += SourceFileInfo(DEFAULT_FILE)
                        input.previous()
                        parseFileInfo(moduleInfo, input)
                    } else {
                        // append line to current source file
                        moduleInfo.sourceFileInfos.last().sourceBuilder.append(line).appendLine()
                    }
                } else {
                    // find new source file
                    moduleInfo.sourceFileInfos += SourceFileInfo(result.groupValues[1])

                    if (result.groupValues[5].isNotBlank() && result.groupValues[6].isNotBlank()) {
                        moduleInfo.entries += Entry(
                            result.groupValues[5],
                            result.groupValues[6],
                            emptyList()
                        )
                    }

                    parseFileInfo(moduleInfo, input)
                }
            }
        }

        fun parseModuleInfo(moduleInfo: SourceModuleInfo, input: ListIterator<String>) {
            while (input.hasNext()) {
                val line = input.next()
                when {
                    line.startsWith(KSP_PREFIX) -> {
                        moduleInfo.symbolProcessorProviders += line.removePrefix(KSP_PREFIX)
                            .split(",").map { it.trim() }
                    }

                    line.startsWith(KSP_ARGS_PREFIX) -> {
                        moduleInfo.kspArgs += line.removePrefix(KSP_PREFIX).split(",").map {
                            it.trim().split("=")
                        }.associate { it[0] to it[1] }
                    }

                    line.startsWith(KAPT_PREFIX) -> {
                        moduleInfo.annotationProcessors += line.removePrefix(KAPT_PREFIX).split(",")
                            .map { it.trim() }
                    }

                    line.startsWith(KAPT_ARGS_PREFIX) -> {
                        moduleInfo.kaptArgs += line.removePrefix(KAPT_ARGS_PREFIX).split(",").map {
                            it.trim().split("=")
                        }.associate { it[0] to it[1] }
                    }

                    line.startsWith(ARGS_PREFIX) -> {
                        line.removePrefix(ARGS_PREFIX).split(",").map {
                            it.trim().split("=")
                        }.associate { it[0] to it[1] }.also {
                            moduleInfo.kaptArgs += it
                            moduleInfo.kspArgs += it
                        }
                    }

                    line.startsWith(KCP_LEGACY_PREFIX) -> {
                        moduleInfo.componentRegistrars += line.removePrefix(KCP_LEGACY_PREFIX).split(",")
                            .map { it.trim() }
                    }

                    line.startsWith(KCP_PREFIX) -> {
                        moduleInfo.compilerPluginRegistrars += line.removePrefix(KCP_PREFIX).split(",")
                            .map { it.trim() }
                    }

                    else -> {
                        input.previous()
                        break
                    }
                }
            }
        }

        fun parseModule(input: ListIterator<String>): SourceModuleInfo {
            val line = input.next()
            val moduleResult = MODULE_NAME_PATTERN.find(line)
            val sourceModuleInfo = if (moduleResult == null) {
                input.previous()
                SourceModuleInfo(DEFAULT_MODULE)
            } else {
                val dependencies = moduleResult.groupValues[3].split(",")
                    .mapNotNull { it.trim().takeIf { it.isNotBlank() } }

                SourceModuleInfo(name = moduleResult.groupValues[1]).apply {
                    this.dependencies += dependencies
                }
            }

            parseModuleInfo(sourceModuleInfo, input)
            parseFiles(sourceModuleInfo, input)
            return sourceModuleInfo
        }

        val moduleInfos = ArrayList<SourceModuleInfo>()
        val iterator = sourceLines.listIterator()
        while (iterator.hasNext()) {
            moduleInfos += parseModule(iterator)
        }
        return moduleInfos
    }

    override fun loadExpectModuleInfos(): Collection<ExpectModuleInfo> {
        return expectLines.fold(ArrayList()) { moduleInfos, line ->
            val moduleResult = MODULE_NAME_PATTERN.find(line)
            if (moduleResult == null) {
                if (moduleInfos.isEmpty()) {
                    moduleInfos += ExpectModuleInfo(DEFAULT_MODULE)
                }

                val result = FILE_NAME_PATTERN.find(line)
                if (result == null) {
                    val currentModule = moduleInfos.last()
                    if (currentModule.sourceFileInfos.isEmpty()) {
                        moduleInfos.last().sourceFileInfos += SourceFileInfo(DEFAULT_FILE)
                    }
                    // append line to current source file
                    currentModule.sourceFileInfos.last().sourceBuilder.append(line).appendLine()
                } else {
                    // find new source file
                    moduleInfos.last().sourceFileInfos += SourceFileInfo(result.groupValues[1])
                }
            } else {
                moduleInfos += ExpectModuleInfo(name = moduleResult.groupValues[1])
            }
            moduleInfos
        }
    }
}