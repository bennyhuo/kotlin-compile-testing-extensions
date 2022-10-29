package com.bennyhuo.kotlin.compiletesting.extensions.module

import com.bennyhuo.kotlin.compiletesting.extensions.result.ResultCollector
import com.bennyhuo.kotlin.compiletesting.extensions.source.ExpectModuleInfo
import com.tschuchort.compiletesting.KotlinCompilation
import kotlin.test.assertEquals

/**
 * Created by benny at 2022/1/3 8:47 AM.
 */
fun Collection<KotlinModule>.resolveAllDependencies() {
    val moduleMap = this.associateBy { it.name }
    forEach {
        it.resolveDependencies(moduleMap)
    }
}

fun Collection<KotlinModule>.compileAll() {
    forEach {
        it.compile()
    }
}

fun KotlinModule.checkResult(
    expectModuleInfo: ExpectModuleInfo,
    checkExitCode: Boolean = true,
    executeEntries: Boolean = false,
    checkGeneratedFiles: Boolean = false,
    checkGeneratedIr: Boolean = false,
    checkCompilerOutput: Boolean = false,
    compilerOutputName: String = "compiles.log",
    compilerOutputLevel: Int = LEVEL_ERROR
) {
    return listOf(this).checkResult(
        listOf(expectModuleInfo),
        checkExitCode,
        executeEntries,
        checkGeneratedFiles,
        checkGeneratedIr,
        checkCompilerOutput,
        compilerOutputName,
        compilerOutputLevel
    )
}

fun Collection<KotlinModule>.checkResult(
    expectModuleInfos: Collection<ExpectModuleInfo>,
    checkExitCode: Boolean = true,
    executeEntries: Boolean = false,
    checkGeneratedFiles: Boolean = false,
    checkGeneratedIr: Boolean = false,
    checkCompilerOutput: Boolean = false,
    compilerOutputName: String = "compiles.log",
    compilerOutputLevel: Int = LEVEL_ERROR
) {
    val resultMap = associate {
        if (checkExitCode) {
            assertEquals(it.compileResult?.exitCode, KotlinCompilation.ExitCode.OK)
        }

        val result = HashMap<String, String>()

        if (checkCompilerOutput) {
            it.compileResult?.let { compilationResult ->
                val compilerOutput = compilationResult.parseOutput().filterOutputs(compilerOutputLevel).toString()
                result[compilerOutputName] = compilerOutput
            }
        }

        if (executeEntries) {
            result += it.runJvm()
        }

        if (checkGeneratedFiles) {
            result += it.generatedSourceDirs.flatMap {
                it.walkTopDown().filter { !it.isDirectory }
            }.associate { it.name to it.readText() }
        }

        it.name to result
    }

    expectModuleInfos.fold(ResultCollector()) { collector, expectModuleInfo ->
        collector.collectModule(expectModuleInfo.name)
        expectModuleInfo.sourceFileInfos.forEach {
            collector.collectFile(it.fileName)
            collector.collectLine(it.content, resultMap[expectModuleInfo.name]?.get(it.fileName))
        }
        collector
    }.apply()
}