package com.bennyhuo.kotlin.compiletesting.extensions.ir

import com.bennyhuo.kotlin.compiletesting.extensions.module.IR_OUTPUT_TYPE_KOTLIN_LIKE
import com.bennyhuo.kotlin.compiletesting.extensions.module.IR_OUTPUT_TYPE_KOTLIN_LIKE_JC
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.util.KotlinLikeDumpOptions
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import java.io.File

/**
 * Created by benny.
 */
internal class IrSourcePrinterRegistrar(outputDir: File) : ComponentRegistrar {

    private val extension = IrSourcePrinterExtension(outputDir)

    var isEnabled: Boolean = false

    var type: Int
        set(value) {
            extension.type = value
        }
        get() = extension.type

    var indentSize: Int
        set(value) {
            extension.indentSize = value
        }
        get() = extension.indentSize

    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        if (isEnabled) {
            IrGenerationExtension.registerExtension(project, extension)
        }
    }
}

class IrSourcePrinterExtension(private val outputDir: File) : IrGenerationExtension {

    var type: Int = IR_OUTPUT_TYPE_KOTLIN_LIKE_JC
    var indentSize: Int = 4

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.files.forEach { irFile ->
            outputDir.resolve(irFile.fqName.asString().replace('.', File.separatorChar)).run {
                mkdirs()
                val source = when(type) {
                    IR_OUTPUT_TYPE_KOTLIN_LIKE_JC -> irFile.dumpSrc(indentSize)
                    IR_OUTPUT_TYPE_KOTLIN_LIKE -> irFile.dumpKotlinLike(KotlinLikeDumpOptions(
                        printFileName = false,
                        printFilePath = false,
                        indentSize = indentSize
                    ))
                    else -> irFile.dump()
                }

                resolve("${irFile.name}.ir").writeText(source)
                irFile.dumpKotlinLike()
            }
        }
    }
}