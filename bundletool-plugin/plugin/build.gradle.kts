plugins {
    id("java-gradle-plugin")
    `kotlin-dsl`
    id("PublishLogic")
}

java {
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.agp.api)
    compileOnly(libs.android.tools.sdklib)
    compileOnly(libs.agp.gradle)
    compileOnly(libs.bundleTool)
    implementation(libs.guava)
    implementation(gradleKotlinDsl())
}

gradlePlugin {
    plugins {
        create("BundleToolPlugin") {
            id = "io.github.xeemoo.bundletool"
            implementationClass = "io.github.xeemoo.BundleToolPlugin"
        }
    }
}