package com.bennyhuo.kotlin.compiletesting.extensions.ir

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.getPackageFragment
import org.jetbrains.kotlin.name.FqName
import java.io.File

/**
 * Created by benny.
 */
class IrSourcePrinterRegistrar(outputDir: File) : ComponentRegistrar {

    private val extension = IrSourcePrinterExtension(outputDir)

    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        IrGenerationExtension.registerExtension(project, extension)
    }
}

class IrSourcePrinterExtension(private val outputDir: File) : IrGenerationExtension {

    init {
        println(outputDir)
    }

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.files.forEach { irFile ->
            val irPackage = irFile.getPackageFragment()
            val packageName = irPackage?.fqName?.asString() ?: ""
            val packageDir = outputDir.resolve(packageName.replace('.', File.separatorChar))
            packageDir.mkdirs()


            packageDir.resolve("${irFile.name}.ir").writer().use {
                if (irPackage?.fqName != FqName.ROOT) {
                    it.appendLine("package $packageName")
                }
                it.write(irFile.dumpSrc())
            }

            println(irFile.dump())
        }
    }
}