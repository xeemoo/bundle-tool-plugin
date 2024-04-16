//buildscript {
//    dependencies {
//        classpath(libs.bundleTool.plugin)
//    }
//}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.androidDynamicFeature) apply false
}