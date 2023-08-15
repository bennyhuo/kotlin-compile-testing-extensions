[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.bennyhuo.kotlin/kotlin-compile-testing-extensions/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.bennyhuo.kotlin/kotlin-compile-testing-extensions)


# kotlin-compile-testing-extensions

This is an extension of [tschuchortdev/kotlin-compile-testing](https://github.com/tschuchortdev/kotlin-compile-testing) by adding module support.

If you want to test ksp or kapt processors on different modules, this library may help.

# How to use

## Set up

I have deployed these modules to maven central, you may add this in your build.gradle: 
 
```
repositories {
    mavenCentral()
}

dependencies {
    testImplementation("com.bennyhuo.kotlin:kotlin-compile-testing-extensions:<lastest-version>")
}
```

## Examples

See the tests in [bennyhuo/Mixin](https://github.com/bennyhuo/Mixin/tree/master/compiler/).

## ChangeLog

### 1.8.20-1.1.0

* Add 'ignoreTrailingSpaces' option to ignore the trailing whitespaces when comparing the test result.
* Add 'TextBasedModuleInfoLoader' to make it possible to load the test sources from text.

### 1.8.20-1.0.0

* Upgrade to Kotlin 1.8.20.
* Add support to add customized output files.

### 1.8.0.1

* Catch exceptions when running the testing code.

### 1.8.0.0

* Upgrade to Kotlin 1.8.0, depends on the fork maintained by [ZacSweers](https://github.com/ZacSweers/kotlin-compile-testing).
* Fix compiler log pattern.
* Add 'exitCode' parameter to 'checkResult'.
* Add 'supportK2' to 'KotlinModule'.

### 1.7.10.2

* Add support to check module results.
* Add support to check IR outputs.
* Add support for JVM entry.

### 1.7.10.1

* Compatible with Java 8/Kotlin 1.7.10.

# License

[MIT License](https://github.com/bennyhuo/kotlin-compile-testing-extensions/blob/master/LICENSE)

    Copyright (c) 2022 Bennyhuo
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.

