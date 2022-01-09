package com.bennyhuo.kotlin.compiletesting.extensions.source

class SourceModuleInfo(
    val name: String,
    val args: Map<String, String> = emptyMap(),
    val dependencies: List<String> = emptyList(),
    val sourceFileInfos: MutableList<SourceFileInfo> = ArrayList(),
    val entries: MutableList<Entry> = ArrayList()
)

class ExpectModuleInfo(
    val name: String,
    val sourceFileInfos: MutableList<SourceFileInfo> = ArrayList()
)

class SourceFileInfo(val fileName: String) {
    val sourceBuilder = StringBuilder()

    override fun toString(): String {
        return "$fileName: \n$sourceBuilder"
    }
}

class Entry(
    val fileName: String,
    val className: String,
    val functionName: String
) {
    override fun toString(): String {
        return "$className#$functionName"
    }
}