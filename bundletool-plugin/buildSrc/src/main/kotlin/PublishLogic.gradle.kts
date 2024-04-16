import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    `java-gradle-plugin`
    `maven-publish`
}

val groupName = "io.github.xeemoo"
val artifactName = "bundletool"
val pluginVersion = "1.0.0"

java {
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

afterEvaluate {
    publishing {
        repositories {
            maven {
                name = "LocalRepo"
                url = uri("${project.buildDir}/repo")
            }
        }

        publications {
            create<MavenPublication>("maven") {
                groupId = groupName
                artifactId = artifactName
                version = pluginVersion

                from(components["java"])
            }
        }
    }
}
