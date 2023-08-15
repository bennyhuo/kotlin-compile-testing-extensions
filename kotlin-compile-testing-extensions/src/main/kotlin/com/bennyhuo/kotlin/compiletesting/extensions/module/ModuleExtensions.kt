package com.bennyhuo.kotlin.compiletesting.extensions.module

import com.bennyhuo.kotlin.compiletesting.extensions.ir.IrSourceOptions
import com.bennyhuo.kotlin.compiletesting.extensions.result.ResultCollector
import com.bennyhuo.kotlin.compiletesting.extensions.source.ExpectModuleInfo
import com.bennyhuo.kotlin.compiletesting.extensions.utils.readTextAndUnify
import com.tschuchort.compiletesting.KotlinCompilation
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.assertEquals

/**
 * Created by benny at 2022/1/3 8:47 AM.
 */
@ExperimentalCompilerApi
internal fun Collection<KotlinModule>.resolveAllDependencies() {
    val moduleMap = this.associateBy { it.name }
    forEach {
        it.resolveDependencies(moduleMap)
    }
}

@ExperimentalCompilerApi
fun Collection<KotlinModule>.compileAll() {
    resolveAllDependencies()
    forEach {
        it.compile()
    }
}

@ExperimentalCompilerApi
fun KotlinModule.checkResult(
    expectModuleInfo: ExpectModuleInfo,
    options: CheckResultOptions = CheckResultOptions()
) {
    listOf(this).checkResult(listOf(expectModuleInfo), options)
}

@ExperimentalCompilerApi
fun Collection<KotlinModule>.checkResult(
    expectModuleInfos: Collection<ExpectModuleInfo>,
    options: CheckResultOptions = CheckResultOptions()
) {
    with(options) {
        if (checkGeneratedIr) {
            check(all { !it.isCompiled }) {
                "Modules should not be compiled before if checkGeneratedIr == true."
            }

            forEach {
                it.sourcePrinter.isEnabled = true
                it.sourcePrinter.options = IrSourceOptions(irOutputType, irSourceIndent)

                it.sourcePrinterLegacy.isEnabled = true
                it.sourcePrinterLegacy.options = IrSourceOptions(irOutputType, irSourceIndent)
            }
        }

        compileAll()
        val resultMap = associate {
            if (checkExitCode) {
                assertEquals(exitCode, it.compileResult?.exitCode)
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

            result += it.customizedOutputDirs.flatMap {
                it.walkTopDown().filter { !it.isDirectory }
            }.associate { it.name to it.readTextAndUnify() }

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
                collector.collect(it.content, resultMap[expectModuleInfo.name]?.get(it.fileName))
            }
            collector
        }.apply(options.ignoreTrailingSpaces)
    }
}

@ExperimentalCompilerApi
fun KotlinModule.checkResult(
    expectModuleInfo: ExpectModuleInfo,
    checkExitCode: Boolean = true,
    exitCode: KotlinCompilation.ExitCode = KotlinCompilation.ExitCode.OK,
    executeEntries: Boolean = false,
    checkGeneratedFiles: Boolean = false,
    checkGeneratedIr: Boolean = false,
    irOutputType: Int = IR_OUTPUT_TYPE_KOTLIN_LIKE_JC,
    irSourceIndent: String = IR_OUTPUT_INDENT_DEFAULT,
    checkCompilerOutput: Boolean = false,
    compilerOutputName: String = "compiles.log",
    compilerOutputLevel: Int = COMPILER_OUTPUT_LEVEL_ERROR,
    ignoreTrailingSpaces: Boolean = true
) {
    listOf(this).checkResult(
        listOf(expectModuleInfo),
        checkExitCode,
        exitCode,
        executeEntries,
        checkGeneratedFiles,
        checkGeneratedIr,
        irOutputType,
        irSourceIndent,
        checkCompilerOutput,
        compilerOutputName,
        compilerOutputLevel,
        ignoreTrailingSpaces
    )
}

@ExperimentalCompilerApi
fun Collection<KotlinModule>.checkResult(
    expectModuleInfos: Collection<ExpectModuleInfo>,
    checkExitCode: Boolean = false,
    exitCode: KotlinCompilation.ExitCode = KotlinCompilation.ExitCode.OK,
    executeEntries: Boolean = false,
    checkGeneratedFiles: Boolean = false,
    checkGeneratedIr: Boolean = false,
    irOutputType: Int = IR_OUTPUT_TYPE_KOTLIN_LIKE_JC,
    irSourceIndent: String = IR_OUTPUT_INDENT_DEFAULT,
    checkCompilerOutput: Boolean = false,
    compilerOutputName: String = "compiles.log",
    compilerOutputLevel: Int = COMPILER_OUTPUT_LEVEL_ERROR,
    ignoreTrailingSpaces: Boolean = true
) {
    checkResult(
        expectModuleInfos,
        CheckResultOptions(
            checkExitCode,
            exitCode,
            executeEntries,
            checkGeneratedFiles,
            checkGeneratedIr,
            irOutputType,
            irSourceIndent,
            checkCompilerOutput,
            compilerOutputName,
            compilerOutputLevel,
            ignoreTrailingSpaces
        )
    )
}
