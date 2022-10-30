package com.bennyhuo.kotlin.compiletesting.extensions.module

import com.bennyhuo.kotlin.compiletesting.extensions.result.ResultCollector
import com.bennyhuo.kotlin.compiletesting.extensions.source.ExpectModuleInfo
import com.bennyhuo.kotlin.compiletesting.extensions.utils.readTextAndUnify
import com.tschuchort.compiletesting.KotlinCompilation
import kotlin.test.assertEquals

/**
 * Created by benny at 2022/1/3 8:47 AM.
 */
internal fun Collection<KotlinModule>.resolveAllDependencies() {
    val moduleMap = this.associateBy { it.name }
    forEach {
        it.resolveDependencies(moduleMap)
    }
}

fun Collection<KotlinModule>.compileAll() {
    resolveAllDependencies()
    forEach {
        it.compile()
    }
}

fun KotlinModule.checkResult(
    expectModuleInfo: ExpectModuleInfo,
    options: CheckResultOptions = CheckResultOptions()
) {
    checkResult(
        expectModuleInfo,
        options.checkExitCode,
        options.executeEntries,
        options.checkGeneratedFiles,
        options.checkGeneratedIr,
        options.irOutputType,
        options.checkCompilerOutput,
        options.compilerOutputName,
        options.compilerOutputLevel,
    )
}

fun Collection<KotlinModule>.checkResult(
    expectModuleInfos: Collection<ExpectModuleInfo>,
    options: CheckResultOptions = CheckResultOptions()
) {
    checkResult(
        expectModuleInfos,
        options.checkExitCode,
        options.executeEntries,
        options.checkGeneratedFiles,
        options.checkGeneratedIr,
        options.irOutputType,
        options.checkCompilerOutput,
        options.compilerOutputName,
        options.compilerOutputLevel,
    )
}

fun KotlinModule.checkResult(
    expectModuleInfo: ExpectModuleInfo,
    checkExitCode: Boolean = true,
    executeEntries: Boolean = false,
    checkGeneratedFiles: Boolean = false,
    checkGeneratedIr: Boolean = false,
    irOutputType: Int = IR_OUTPUT_TYPE_KOTLIN_LIKE_JC,
    checkCompilerOutput: Boolean = false,
    compilerOutputName: String = "compiles.log",
    compilerOutputLevel: Int = COMPILER_OUTPUT_LEVEL_ERROR
) {
    listOf(this).checkResult(
        listOf(expectModuleInfo),
        checkExitCode,
        executeEntries,
        checkGeneratedFiles,
        checkGeneratedIr,
        irOutputType,
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
    irOutputType: Int = IR_OUTPUT_TYPE_KOTLIN_LIKE_JC,
    checkCompilerOutput: Boolean = false,
    compilerOutputName: String = "compiles.log",
    compilerOutputLevel: Int = COMPILER_OUTPUT_LEVEL_ERROR
) {
    if (checkGeneratedIr) {
        check(all { !it.isCompiled }) {
            "Modules should not be compiled before if checkGeneratedIr == true."
        }

        forEach {
            it.sourcePrinter.isEnabled = true
            it.sourcePrinter.type = irOutputType
        }
    }

    compileAll()
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
            }.associate { it.name to it.readTextAndUnify() }
        }

        if (checkGeneratedIr) {
            result += it.irTransformedSourceDir.walkTopDown().filter {
                !it.isDirectory
            }.associate { it.name to it.readTextAndUnify() }
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