package com.bennyhuo.kotlin.compiletesting.extensions.utils

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

/**
 * Created by benny at 2022/1/8 6:26 PM.
 */
internal inline fun captureStdOut(block: () -> Unit): String {
    val originalStdOut = System.out
    val stdOutStream = ByteArrayOutputStream()
    System.setOut(PrintStream(stdOutStream))
    try {
        block()
    } finally {
        System.setOut(originalStdOut)
    }
    return stdOutStream.toString().unify()
}

internal fun String.unify() = replace("\r\n", "\n").trimEnd()

internal fun File.readTextAndUnify() = readText().unify()