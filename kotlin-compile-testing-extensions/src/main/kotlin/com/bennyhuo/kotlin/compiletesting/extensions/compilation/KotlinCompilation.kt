package com.bennyhuo.kotlin.compiletesting.extensions.compilation

import com.bennyhuo.kotlin.compiletesting.extensions.source.Entry
import com.bennyhuo.kotlin.compiletesting.extensions.utils.captureStdOut
import com.tschuchort.compiletesting.KotlinCompilation
import java.lang.reflect.Modifier

/**
 * Created by benny.
 */
fun KotlinCompilation.Result.runJvm(
    entry: Entry,
    classLoader: ClassLoader = this.classLoader
): String {
    return if (exitCode == KotlinCompilation.ExitCode.OK) {
        captureStdOut {
            val entryClass = classLoader.loadClass(entry.className)
            val entryFunction = entryClass.getDeclaredMethod(entry.functionName)
            check(Modifier.isStatic(entryFunction.modifiers)) {
                "Entry function $entryFunction must be static."
            }
            check(Modifier.isPublic(entryFunction.modifiers)) {
                "Entry function $entryFunction must be public."
            }
            check(entryFunction.returnType == Void.TYPE) {
                "Entry function $entryFunction should return void."
            }
            check(entryFunction.parameterCount == 0) {
                "Entry function cannot have parameters."
            }
            entryFunction.invoke(null)
        }
    } else ""
}