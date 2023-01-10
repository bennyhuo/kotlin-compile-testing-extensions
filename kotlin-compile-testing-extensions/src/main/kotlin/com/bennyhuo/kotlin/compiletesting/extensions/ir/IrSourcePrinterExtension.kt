package com.bennyhuo.kotlin.compiletesting.extensions.ir

import com.bennyhuo.kotlin.compiletesting.extensions.module.IR_OUTPUT_INDENT_DEFAULT
import com.bennyhuo.kotlin.compiletesting.extensions.module.IR_OUTPUT_TYPE_KOTLIN_LIKE
import com.bennyhuo.kotlin.compiletesting.extensions.module.IR_OUTPUT_TYPE_KOTLIN_LIKE_JC
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import java.io.File

/**
 * Created by benny.
 */
internal class IrSourceOptions(
    var type: Int = IR_OUTPUT_TYPE_KOTLIN_LIKE_JC,
    var indent: String = IR_OUTPUT_INDENT_DEFAULT
)

@Deprecated(
    message = "IrSourcePrinterLegacyRegistrar is deprecated. Please use IrSourcePrinterRegistrar instead.",
    replaceWith = ReplaceWith("IrSourcePrinterRegistrar"),
    level = DeprecationLevel.WARNING
)
@ExperimentalCompilerApi
internal class IrSourcePrinterLegacyRegistrar(outputDir: File) : ComponentRegistrar {

    var isEnabled: Boolean = false

    var options = IrSourceOptions()

    private val extension = IrSourcePrinterExtension(outputDir, options)

    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        if (isEnabled) {
            IrGenerationExtension.registerExtension(project, extension)
        }
    }
}

@ExperimentalCompilerApi
internal class IrSourcePrinterRegistrar(outputDir: File) : CompilerPluginRegistrar() {

    var isEnabled: Boolean = false

    var options = IrSourceOptions()

    private val extension = IrSourcePrinterExtension(outputDir, options)

    override val supportsK2: Boolean = false
    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (isEnabled) {
            IrGenerationExtension.registerExtension(extension)
        }
    }
}

internal class IrSourcePrinterExtension(
    private val outputDir: File,
    private val options: IrSourceOptions
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.files.forEach { irFile ->
            outputDir.resolve(irFile.fqName.asString().replace('.', File.separatorChar)).run {
                mkdirs()
                val source = when (options.type) {
                    IR_OUTPUT_TYPE_KOTLIN_LIKE_JC -> irFile.dumpSrc(options.indent)
                    IR_OUTPUT_TYPE_KOTLIN_LIKE -> irFile.dumpKotlinLike(
                        KotlinLikeDumpOptions(
                            printFileName = false,
                            printFilePath = false,
                            indent = options.indent
                        )
                    )

                    else -> irFile.dump()
                }

                resolve("${irFile.name}.ir").writeText(source)
                irFile.dumpKotlinLike()
            }
        }
    }
}