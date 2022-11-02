package com.bennyhuo.kotlin.compiletesting.extensions.utils

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

/**
 * Created by benny at 2022/1/8 6:26 PM.
 */
internal inline fun captureStdOut(block: () -> Unit): String {
    val originalStdOut = System.out
    val originalStdErr = System.err
    val stdOutStream = ByteArrayOutputStream()
    val printStream = PrintStream(stdOutStream)
    System.setOut(printStream)
    System.setErr(printStream)
    try {
        block()
    } finally {
        System.setOut(originalStdOut)
        System.setErr(originalStdErr)
    }
    return stdOutStream.toString().unify()
}

internal fun String.unify() = replace("\r\n", "\n").trimEnd()

internal fun File.readTextAndUnify() = readText().unify()