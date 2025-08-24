# Printer Changes

Copy the original source code from Kotlin compiler and apply these changes.

TODO: Create a git patch in the next time.

## builtin

```kotlin
val printFakeOverrideDeclarations: Boolean = true,
val indent: String = IR_OUTPUT_INDENT_DEFAULT
```

```kotlin
override fun visitSimpleFunction(declaration: IrSimpleFunction, data: IrDeclaration?) {
    if (declaration.origin == IrDeclarationOrigin.FAKE_OVERRIDE) return
```

## compose


```kotlin
//     override fun visitClass(declaration: IrClass) {

        val superTypesWithoutAny = declaration.superTypes.filter { !it.isAny() }
        if (superTypesWithoutAny.isNotEmpty()) {
            print(": ")
            print(superTypesWithoutAny.joinToString(", ") { it.renderSrc() })
            print(" ")
        }
```

```kotlin
//     fun IrClass.printAsObject() {
val superTypesWithoutAny = superTypes.filter { !it.isAny() }
if (superTypesWithoutAny.isNotEmpty()) {
    print(": ")
    print(superTypesWithoutAny.joinToString(", ") { it.renderSrc() })
    print(" ")
}
```

```kotlin
    override fun visitPackageFragment(declaration: IrPackageFragment) {
        if (declaration.fqName != FqName.ROOT) {
            println("package ${declaration.fqName.asString()}")
            println()
        }
    }

    override fun visitFile(declaration: IrFile) {
        includeFileNameInExceptionTrace(declaration) {
            // println("// FILE: ${declaration.fileEntry.name}")
            visitPackageFragment(declaration)
            declaration.declarations.printJoin("\n")
            "\r\b\n"
        }
    }
```

```kotlin
    override fun visitStringConcatenation(expression: IrStringConcatenation) {
        val arguments = expression.arguments
        val rawStringPreferred = arguments.mapNotNull {
            (it as? IrConst<*>)?.value?.toString()?.rawStringPreferred()
        }.let {
            it.isNotEmpty() && it.all { it }
        }

        val quote = if (rawStringPreferred) "\"\"\"" else "\""
        print(quote)
        for (argument in arguments) {
            when {
                argument is IrConst<*> && argument.kind == IrConstKind.String -> {
                    if (rawStringPreferred) {
                        print(argument.value.toString())
                    } else {
                        print(argument.value.toString().escapeCharacters())
                    }
                }
                argument is IrGetValue -> {
                    print("$")
                    argument.print()
                }
                else -> {
                    print("\${")
                    argument.print()
                    print("}")
                }
            }
        }
        print(quote)
    }

```

```kotlin
//     override fun visitConst(expression: IrConst<*>) {
            is IrConstKind.String -> {
                val value = expression.value.toString()
                if (value.rawStringPreferred() == true) {
                    "\"\"\"$value\"\"\""
                } else {
                    "\"${StringUtil.escapeCharCharacters(expression.value.toString())}\""
                }
            }
```