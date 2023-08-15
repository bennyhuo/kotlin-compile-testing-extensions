package com.bennyhuo.kotlin.compiletesting.extensions.result

import kotlin.test.assertEquals

/**
 * Created by benny at 2022/1/9 11:19 AM.
 */
internal class ResultCollector {
    private val expect = ArrayList<String>()
    private val actual = ArrayList<String>()

    fun collect(value: Any?) {
        collect(value, value)
    }

    fun collect(expect: Any?, actual: Any?) {
        this.expect.addAll(expect.toString().lines())
        this.actual.addAll(actual.toString().lines())
    }

    fun collectModule(moduleName: String) {
        collect("// MODULE: $moduleName")
    }

    fun collectFile(fileName: String) {
        collect("// FILE: $fileName")
    }

    fun apply(ignoreTrailingSpaces: Boolean = true) {
        assertEquals(
            expect.joinToString("\n") {
                it.trimEnd { char ->
                    (ignoreTrailingSpaces && char.isWhitespace()) || char == '\r' || char == '\n'
                }
            },
            actual.joinToString("\n") {
                it.trimEnd { char ->
                    (ignoreTrailingSpaces && char.isWhitespace()) || char == '\r' || char == '\n'
                }
            }
        )
    }
}