package com.bennyhuo.kotlin.compiletesting.extensions.module

/**
 * Created by benny at 2022/1/3 8:47 AM.
 */
fun Collection<KotlinModule>.resolveAllDependencies() {
    val moduleMap = this.associateBy { it.name }
    forEach {
        it.resolveDependencies(moduleMap)
    }
}

fun Collection<KotlinModule>.compileAll() {
    forEach {
        it.compile()
    }
}