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
            val entryFunction = entryClass.declaredMethods.singleOrNull {
                it.name == entry.functionName && it.parameterCount == entry.args.size
            }

            check(entryFunction != null) {
                "Cannot find method '${entry.functionName}' in '${entry.className}' with ${entry.args.size} parameters."
            }

            check(Modifier.isStatic(entryFunction.modifiers)) {
                "Entry function $entryFunction must be static."
            }
            check(Modifier.isPublic(entryFunction.modifiers)) {
                "Entry function $entryFunction must be public."
            }
            check(entryFunction.returnType == Void.TYPE) {
                "Entry function $entryFunction should return void."
            }

            val args = entryFunction.parameterTypes.zip(entry.args) {
                type, arg -> type.valueOf(arg)
            }.toTypedArray()

            entryFunction.invoke(null, *args)
        }
    } else ""
}

private fun <T> Class<T>.valueOf(value: String): T? {
    if (value == "null") return null
    return when(this) {
        Boolean::class.java -> value.toBoolean()
        Char::class.java -> value[0]
        Short::class.java -> value.toShort()
        Int::class.java -> value.toInt()
        Long::class.java -> value.toLong()
        Float::class.java -> value.toFloat()
        Double::class.java -> value.toDouble()
        String::class.java -> value
        else -> throw IllegalArgumentException("Parameter type $this is not supported.")
    } as T
}