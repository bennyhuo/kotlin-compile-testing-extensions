package com.bennyhuo.kotlin.compiletesting.extensions.utils

import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Created by benny at 2022/1/8 6:26 PM.
 */
inline fun captureStdOut(block: () -> Unit): String {
    val originalStdOut = System.out
    val stdOutStream = ByteArrayOutputStream()
    System.setOut(PrintStream(stdOutStream))
    try {
        block()
    } finally {
        System.setOut(originalStdOut)
    }
    return stdOutStream.toString()
}