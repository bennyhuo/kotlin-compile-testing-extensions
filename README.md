[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)


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
    testImplementation("com.bennyhuo.kotlin:kotlin-compile-testing-extensions:0.2")
}
```

## Examples

See the tests in [bennyhuo/Mixin](https://github.com/bennyhuo/Mixin/tree/master/compiler/).

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

