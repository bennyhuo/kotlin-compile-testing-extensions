package com.bennyhuo.kotlin.compiletesting.extensions.module

/**
 * Created by benny.
 */

const val COMPILER_OUTPUT_LEVEL_INFO = 1
const val COMPILER_OUTPUT_LEVEL_WARN = 2
const val COMPILER_OUTPUT_LEVEL_ERROR = 3

/**
 * Print IR declarations directly.
 * see [org.jetbrains.kotlin.ir.util.dump]
 */
const val IR_OUTPUT_TYPE_RAW = 0

/**
 * Use Kotlin builtin source printer.
 * see [org.jetbrains.kotlin.ir.util.dumpKotlinLike]
 */
const val IR_OUTPUT_TYPE_KOTLIN_LIKE = 1

/**
 * Use the source printer from Jetpack Compose.
 * It looks better than the Kotlin builtin printer.
 * see [com.bennyhuo.kotlin.compiletesting.extensions.ir.dumpSrc]
 */
const val IR_OUTPUT_TYPE_KOTLIN_LIKE_JC = 2

const val IR_OUTPUT_INDENT_DEFAULT = "    "

class CheckResultOptions(
    val checkExitCode: Boolean = true,
    val executeEntries: Boolean = false,
    val checkGeneratedFiles: Boolean = false,
    val checkGeneratedIr: Boolean = false,
    val irOutputType: Int = IR_OUTPUT_TYPE_KOTLIN_LIKE_JC,
    val irSourceIndent: String = IR_OUTPUT_INDENT_DEFAULT,
    val checkCompilerOutput: Boolean = false,
    val compilerOutputName: String = "compiles.log",
    val compilerOutputLevel: Int = COMPILER_OUTPUT_LEVEL_ERROR
)