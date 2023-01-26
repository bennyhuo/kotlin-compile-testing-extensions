package com.bennyhuo.kotlin.compiletesting.extensions

import com.bennyhuo.kotlin.compiletesting.extensions.module.compileLogPattern
import com.bennyhuo.kotlin.compiletesting.extensions.module.parseOutput
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Created by benny.
 */
@OptIn(ExperimentalCompilerApi::class)
class CompileLogPatternTest {

    @Test
    fun testKaptLog() {
        val windowsPathLog = "e: C:\\path\\to\\Project.java:11: error: Detect infinite copy loop. " +
                "It will cause stack overflow to call Project.deepCopy() in the runtime."
        assertTrue(compileLogPattern.matches(windowsPathLog))

        val unixPathLog = "e: /path/to/Project.java:11: error: Detect infinite copy loop. " +
                "It will cause stack overflow to call Project.deepCopy() in the runtime."
        assertTrue(compileLogPattern.matches(unixPathLog))

        assertEquals(
            "e: Project.java: (11, -1): error: Detect infinite copy loop. It will cause stack overflow to call Project.deepCopy() in the runtime.",
            unixPathLog.parseOutput().first().toString()
        )
    }

    @Test
    fun testKotlinCompilerLog() {
        val log = "w: file:///path/to/Main.kt:5:67 'A' should " +
                "implement 'com.bennyhuo.kotlin.deepcopy.DeepCopyable<T>' to support deep copy."
        assertTrue(compileLogPattern.matches(log))

        assertEquals(
            "w: Main.kt: (5, 67): 'A' should implement 'com.bennyhuo.kotlin.deepcopy.DeepCopyable<T>' to support deep copy.",
            log.parseOutput().first().toString()
        )
    }

}