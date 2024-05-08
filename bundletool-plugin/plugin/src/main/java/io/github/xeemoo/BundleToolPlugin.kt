package io.github.xeemoo

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.impl.VariantImpl
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.component.ApplicationCreationConfig
import com.android.build.gradle.internal.signing.SigningConfigDataProvider
import com.android.sdklib.BuildToolInfo
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.io.File
import java.util.Locale

class BundleToolPlugin : Plugin<Project> {

    private val extName = "bundletool"

    override fun apply(project: Project) {
        project.extensions.create<BundleToolExtension>(extName)

        project.plugins.withType(AppPlugin::class.java) {

            val androidComponents =
                project.extensions.getByType(AndroidComponentsExtension::class.java)

            androidComponents.onVariants { variant ->

                val extension = project.extensions.getByName(extName) as? BundleToolExtension
                if (extension == null || !extension.enable) {
                    return@onVariants
                }

                val deviceSpecFile = if (extension.enableGetSize) {
                    val deviceSpecFile = extension.deviceSpec?.let {
                        project.layout.projectDirectory.file(it).asFile
                    }
                    if (deviceSpecFile?.exists() == false) {
                        throw IllegalArgumentException("deviceSpec can not be null when enable get size task.")
                    }
                    deviceSpecFile
                } else {
                    null
                }

                // booster中提供的方案
                val buildTool =
                    (variant as VariantImpl<*>).global.versionedSdkLoader.get().buildToolInfoProvider.get()
                val aapt2Path = buildTool.getPath(BuildToolInfo.PathId.AAPT2)

                val taskName = "transformApkFromBundleFor${
                    variant.name.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    }
                }"
                project.tasks.register<AabConvertTask>(taskName) {
                    bundle.set(variant.artifacts.get(SingleArtifact.BUNDLE))
                    output.set(
                        project.layout.buildDirectory.dir("intermediates/$taskName")
                    )
                    aapt2.set(File(aapt2Path))
                    deviceSpecFile?.let { deviceSpec.set(it) }
                    signConfigData = SigningConfigDataProvider.create(variant as ApplicationCreationConfig)
                    enableGetSize = extension.enableGetSize
                }
            }
        }
    }

}