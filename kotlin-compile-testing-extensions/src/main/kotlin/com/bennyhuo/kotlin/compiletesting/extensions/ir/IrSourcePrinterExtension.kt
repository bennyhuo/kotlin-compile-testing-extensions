package com.bennyhuo.kotlin.compiletesting.extensions.ir

import com.bennyhuo.kotlin.compiletesting.extensions.module.IR_OUTPUT_TYPE_KOTLIN_LIKE
import com.bennyhuo.kotlin.compiletesting.extensions.module.IR_OUTPUT_TYPE_KOTLIN_LIKE_JC
import com.bennyhuo.kotlin.source.printer.builtin.KotlinLikeDumpOptions
import com.bennyhuo.kotlin.source.printer.builtin.dumpKotlinLike
import com.bennyhuo.kotlin.source.printer.common.IR_OUTPUT_INDENT_DEFAULT
import com.bennyhuo.kotlin.source.printer.compose.dumpSrc
import java.io.File
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.util.dump

/**
 * Created by benny.
 */
internal class IrSourceOptions(
    var type: Int = IR_OUTPUT_TYPE_KOTLIN_LIKE_JC,
    var indent: String = IR_OUTPUT_INDENT_DEFAULT
)

@ExperimentalCompilerApi
internal class IrSourcePrinterRegistrar(outputDir: File) : CompilerPluginRegistrar() {

    private val extension = IrSourcePrinterExtension(outputDir)

    var isEnabled: Boolean = false

    var options by extension::options

    override val supportsK2: Boolean = false
    override val pluginId: String = "com.bennyhuo.kotlin.compiletesting.extensions.ir.IrSourcePrinterRegistrar"

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (isEnabled) {
            IrGenerationExtension.registerExtension(extension)
        }
    }
}

internal class IrSourcePrinterExtension(private val outputDir: File) : IrGenerationExtension {

    var options: IrSourceOptions = IrSourceOptions()

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.files.forEach { irFile ->
            outputDir.resolve(irFile.packageFqName.asString().replace('.', File.separatorChar)).run {
                mkdirs()
                val source = when (options.type) {
                    IR_OUTPUT_TYPE_KOTLIN_LIKE_JC -> irFile.dumpSrc(options.indent)
                    IR_OUTPUT_TYPE_KOTLIN_LIKE -> irFile.dumpKotlinLike(
                        KotlinLikeDumpOptions(
                            printFileName = false,
                            printFilePath = false,
                            indent = options.indent,
                            printFakeOverrideDeclarations = false
                        )
                    )

                    else -> {
                        val path = irFile.path
                        val name = File(path).name
                        irFile.dump().replace(path, name)
                    }
                }

                resolve("${irFile.name}.ir").writeText(source)
                irFile.dumpKotlinLike()
            }
        }
    }
}
