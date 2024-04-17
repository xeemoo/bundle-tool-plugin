package io.github.xeemoo

import com.android.build.gradle.internal.signing.SigningConfigData
import com.android.build.gradle.internal.signing.SigningConfigDataProvider
import com.android.tools.build.bundletool.androidtools.Aapt2Command
import com.android.tools.build.bundletool.commands.BuildApksCommand
import com.android.tools.build.bundletool.commands.BuildApksCommand.ApkBuildMode
import com.android.tools.build.bundletool.commands.GetSizeCommand
import com.android.tools.build.bundletool.model.GetSizeRequest
import com.android.tools.build.bundletool.model.Password
import com.android.tools.build.bundletool.model.SigningConfiguration
import com.android.tools.build.bundletool.model.exceptions.CommandExecutionException
import com.google.common.collect.ImmutableSet
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.PrintStream
import java.nio.file.Path
import java.security.KeyStore
import java.util.Optional
import java.util.zip.ZipFile

abstract class AabConvertTask : DefaultTask() {

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    @get:InputFile
    abstract val bundle: RegularFileProperty

    @get:InputFile
    abstract val aapt2: RegularFileProperty

    @get:Nested
    lateinit var signConfigData: SigningConfigDataProvider

    @get:Input
    var enableGetSize: Boolean = false

    @get:InputFile
    abstract val deviceSpec: RegularFileProperty

    @TaskAction
    fun taskAction() {

        val inputAabFile = bundle.get().asFile
        if (!inputAabFile.name.endsWith(".aab")) {
            throw RuntimeException("Expected bundle file to have .aab extension.")
        }

        val outputDir = output.get().asFile
        val apksOutputFile = File(outputDir, "universal.apks")

        val aapt2Path = aapt2.get().asFile.toPath()

        val sign = signConfigData.resolve()

        val universalMode = ApkBuildMode.UNIVERSAL
        val buildApksCmd = buildApksCommand(inputAabFile, apksOutputFile, aapt2Path, sign, universalMode)
        try {
            buildApksCmd.build().execute()
        } catch (ex: CommandExecutionException) {
            if (ex.localizedMessage.contains("aapt2")) {
                println("AAPT2 command failed. It is recommended that you specify an updated buildToolsVersion in your project. " +
                        "See https://developer.android.com/tools/releases/build-tools")
            }
            throw ex
        }
        println("buildUniversalApks Success.")

        val universalApk = File(apksOutputFile.parentFile, "universal.apk")
        ZipFile(apksOutputFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    if (entry.name.endsWith(".apk")) {
                        universalApk.outputStream().use { input.copyTo(it) }
                    }
                }
            }
        }
        println("unzip apks Success.")


       // !!! ApkBuildMode是UNIVERSA时，是无法获取apk安装大小的，需要额外转一个非UNIVERSA的apks来计算大小

        if (enableGetSize) {
            val calcSizeOutputFile = File(outputDir, "calc_size.apks")
            val defaultMode = ApkBuildMode.DEFAULT
            buildApksCommand(inputAabFile, calcSizeOutputFile, aapt2Path, sign, defaultMode).build().execute()
            println("buildCalcSizeApks Success.")

            val deviceSpecPath = deviceSpec.get().asFile.toPath()

            val getSizeCmd = GetSizeCommand.builder().apply {
                setApksArchivePath(calcSizeOutputFile.toPath())
                setGetSizeSubCommand(GetSizeCommand.GetSizeSubcommand.TOTAL)
                setDimensions(
                    ImmutableSet.of(
                        GetSizeRequest.Dimension.ABI,
                        GetSizeRequest.Dimension.SCREEN_DENSITY
                    )
                )
                setModules(ImmutableSet.of("base"))
                setDeviceSpec(deviceSpecPath)
                setInstant(false)
            }
            val sizeOutFile = File(outputDir, "size.csv")
            sizeOutFile.createNewFile()
            getSizeCmd.build().getSizeTotal(PrintStream(sizeOutFile.outputStream()))
            println("get size success.")
        }
    }

    private fun buildApksCommand(
        inputAabFile: File,
        apksOutputFile: File,
        aapt2Path: Path,
        sign: SigningConfigData?,
        apkBuildMode: ApkBuildMode
    ) = BuildApksCommand.builder().apply {
        setBundlePath(inputAabFile.toPath())
        setOutputFile(apksOutputFile.toPath())
        setOverwriteOutput(true)
        setAapt2Command(Aapt2Command.createFromExecutablePath(aapt2Path))
        setApkBuildMode(apkBuildMode)
        sign?.takeIf { it.storeFile != null }?.let { signData ->
            setSigningConfiguration(
                SigningConfiguration.extractFromKeystore(
                    signData.storeFile?.toPath(),
                    signData.keyAlias,
                    Optional.ofNullable(
                        signData.storePassword?.let {
                            Password { KeyStore.PasswordProtection(it.toCharArray()) }
                        }
                    ),
                    Optional.ofNullable(
                        signData.keyPassword?.let {
                            Password { KeyStore.PasswordProtection(it.toCharArray()) }
                        }
                    )
                )
            )
        }
    }

}