# BundleToolPlugin

The tool of convert aab to apk and more, powered by [bundletool](https://github.com/google/bundletool).

[![License](https://img.shields.io/badge/license-Apache2.0-brightgreen)](LICENSE)
[![Workflow](https://github.com/xeemoo/bundle-tool-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/xeemoo/bundle-tool-plugin/actions)
[![Version](https://img.shields.io/badge/latest-1.0.0-blue)](https://github.com/xeemoo/bundle-tool-plugin/tree/mvn-repo/io/github/xeemoo/bundletool)

## Feature
- generate `universal apk` from aab
- calculate download size from aab


## Quick start
1. add maven repo in `settings.gradle(.kts)`
```kotlin
pluginManagement {
    repositories {
        maven { url 'https://raw.githubusercontent.com/xeemoo/bundle-tool-plugin/mvn-repo' }
    }
}
```

2. configured in root project `libs.versions.toml` and `build.gradle(.kts)`
```toml
[versions]
bundleToolPlugin = "latest_version"
[libraries]
bundleToolPlugin = { group = "io.github.xeemoo", name = "bundletool", version.ref = "bundleToolPlugin" }
```

```kotlin
buildscript {
    dependencies {
        classpath libs.bundleToolPlugin
    }
}
```

3. configured in application `build.gradle(.kts)`
```kotlin
plugins {
    id 'io.github.xeemoo.bundletool'
}

bundletool {
    enable true
    enableGetSize true
    deviceSpec "path/to/device_spec.json"
}
```

4. exec `gradlew :application_module:transformApkFromBundleForDebug`

5. after build, get output in `application_module/build/intermediates/transformApkFromBundleForDebug` folder