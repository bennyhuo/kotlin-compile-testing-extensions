package com.bennyhuo.kotlin.compiletesting.extensions.module

import com.tschuchort.compiletesting.KotlinCompilation
import java.io.File



private val compileLogPattern = Regex("([iew]): .*/(.*): \\((\\d+), (\\d+)\\): (.*)")

fun KotlinCompilation.Result.parseOutput(): CompilerOutput {
    return compileLogPattern.findAll(messages).map { result ->
        CompilerOutputLine(
            result.groupValues[1],
            result.groupValues[2],
            result.groupValues[3].toIntOrNull() ?: -1,
            result.groupValues[4].toIntOrNull() ?: -1,
            result.groupValues[5],
        )
    }.let {
        CompilerOutput(exitCode, outputs = it.toList())
    }
}


class CompilerOutput(
    val code: KotlinCompilation.ExitCode,
    val outputs: List<CompilerOutputLine>
) {

    fun filterOutputs(level: Int) = CompilerOutput(code, outputs.filter { it.levelInt == level })

    fun errors() = filterOutputs(COMPILER_OUTPUT_LEVEL_ERROR)
    fun infos() = filterOutputs(COMPILER_OUTPUT_LEVEL_INFO)
    fun warns() = filterOutputs(COMPILER_OUTPUT_LEVEL_WARN)

    override fun toString(): String {
        return if (outputs.isEmpty()) code.toString()
        else "$code\n${outputs.joinToString("\n")}"
    }
}

class CompilerOutputLine(
    val level: String,
    val path: String,
    val offsetStart: Int,
    val offsetEnd: Int,
    val message: String
) {

    val levelInt = when (level) {
        "i" -> COMPILER_OUTPUT_LEVEL_INFO
        "w" -> COMPILER_OUTPUT_LEVEL_WARN
        "e" -> COMPILER_OUTPUT_LEVEL_ERROR
        else -> COMPILER_OUTPUT_LEVEL_INFO
    }

    override fun toString() = "$level: ${File(path).name}: ($offsetStart, $offsetEnd): $message"
}
