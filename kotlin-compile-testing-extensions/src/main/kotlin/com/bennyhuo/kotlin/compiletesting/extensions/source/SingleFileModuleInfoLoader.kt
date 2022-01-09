package com.bennyhuo.kotlin.compiletesting.extensions.source

import java.io.File

/**
 * Created by benny at 2022/1/7 10:29 AM.
 */
private const val SOURCE_START_LINE = "// SOURCE"
private const val GENERATED_START_LINE = "// GENERATED"
private val FILE_NAME_PATTERN = Regex("""// FILE: ((\w+)\.(\w+))\s*(\[(\S*)#(\S*)])?""")
private val MODULE_NAME_PATTERN = Regex("""// MODULE: ([-\w]+)(\s*/\s*(([-\w]+)(\s*,\s*([-\w]+))*))?\s*(#(.*))?""")

private const val DEFAULT_MODULE = "default-module"
private const val DEFAULT_FILE = "DefaultFile.kt"

class SingleFileModuleInfoLoader(private val filePath: String) : ModuleInfoLoader {

    private val lines by lazy {
        File(filePath).readLines().dropWhile { it.trim() != SOURCE_START_LINE }
    }

    private val sourceLines by lazy {
        lines.takeWhile { it.trim() != GENERATED_START_LINE }.drop(1)
    }

    private val expectLines by lazy {
        lines.dropWhile { it.trim() != GENERATED_START_LINE }.drop(1)
    }

    override fun loadSourceModuleInfos(): Collection<SourceModuleInfo> {
        return sourceLines.fold(ArrayList()) { moduleInfos, line ->
            val moduleResult = MODULE_NAME_PATTERN.find(line)
            if (moduleResult == null) {
                if (moduleInfos.isEmpty()) {
                    moduleInfos += SourceModuleInfo(DEFAULT_MODULE)
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
                    val currentModule = moduleInfos.last()
                    currentModule.sourceFileInfos += SourceFileInfo(result.groupValues[1])

                    if (result.groupValues[5].isNotBlank() && result.groupValues[6].isNotBlank()) {
                        currentModule.entries += Entry(
                            result.groupValues[1],
                            result.groupValues[5],
                            result.groupValues[6]
                        )
                    }
                }
            } else {
                val dependencies = moduleResult.groupValues[3].split(",")
                    .mapNotNull { it.trim().takeIf { it.isNotBlank() } }

                val args = moduleResult.groupValues[8].split(",").mapNotNull {
                    it.trim().split(":").takeIf { it.size == 2 }
                }.associate { it[0] to it[1] }


                moduleInfos += SourceModuleInfo(
                    name = moduleResult.groupValues[1],
                    args = args,
                    dependencies = dependencies
                )
            }
            moduleInfos
        }
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