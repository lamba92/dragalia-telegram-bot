@file:Suppress("SuspiciousCollectionReassignment")

import com.github.lamba92.gradle.utils.TRAVIS_TAG
import com.github.lamba92.gradle.utils.lamba
import org.gradle.internal.os.OperatingSystem
import java.io.ByteArrayOutputStream

buildscript {
    repositories {
        maven("https://dl.bintray.com/lamba92/com.github.lamba92")
        google()
    }
    dependencies {
        classpath("com.github.lamba92", "lamba-gradle-utils", "1.0.6")
    }
}

plugins {
    kotlin("jvm") version "1.4-M1"
    id("org.hidetake.ssh") version "2.10.1"
    application
}

group = "com.github.lamba92"
version = TRAVIS_TAG ?: "0.0.1"

repositories {
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
    maven("https://dl.bintray.com/lamba92/com.github.lamba92")
    maven("https://dl.bintray.com/kotlin/kotlinx.html")
    jcenter()
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

kotlin.target.compilations.all {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.telegram", "telegrambots", "4.4.0.1")
    implementation(lamba("dragalia-library-kodein-di", "+"))
    implementation(lamba("telegrambots-ktx", "+"))
    implementation("com.vdurmont", "emoji-java", "5.1.1")
}

application {
    mainClassName = "com.github.lamba92.dragalialost.bot.MainKt"
}

// Check OS first, if using Win10Home this exec can take a lot of time
// due to Docker Toolbox under VirtualBox cold start
val shouldSetupDocker = if (OperatingSystem.current().isLinux)
    exec {
        commandLine("docker")
        standardOutput = ByteArrayOutputStream()
        errorOutput = ByteArrayOutputStream()
    }.exitValue == 0
else
    false

if (shouldSetupDocker)
    tasks {

        val distTar by getting(Tar::class)

        val dockerBuildFolder = file("$buildDir/dockerBuild").absolutePath

        val copyDistTar by creating(Copy::class) {
            dependsOn(distTar)
            group = "docker"
            from(distTar.archiveFile.get())
            into(dockerBuildFolder)
        }

        val copyDockerfile by creating(Copy::class) {
            group = "docker"
            from("$projectDir/Dockerfile")
            into(dockerBuildFolder)
        }

        fun commands(withVersion: Boolean = false, addArm32: Boolean = true) = arrayOf(
            "docker", "buildx", "build", "-t",
            buildString {
                append("lamba92/${project.name}")
                if (withVersion)
                    append(":${project.version}")
            },
            "--build-arg=TAR_NAME=${distTar.archiveFile.get().asFile.nameWithoutExtension}",
            "--build-arg=APP_NAME=${project.name}",
            buildString {
                append("--platform=linux/amd64,linux/arm64")
                if (addArm32)
                    append(",linux/arm")
            },
            dockerBuildFolder
        )

        val buildMultiArchImages by creating(Exec::class) {
            dependsOn(copyDistTar, copyDockerfile)
            group = "docker"
            commandLine(*commands())
        }

        "build" {
            dependsOn(buildMultiArchImages)
        }

        val publishMultiArchImagesWithLatestTag by creating(Exec::class) {
            dependsOn(copyDistTar, copyDockerfile)
            group = "docker"
            commandLine(*commands(), "--push")
        }

        create<Exec>("publishMultiArchImages") {
            dependsOn(publishMultiArchImagesWithLatestTag)
            group = "docker"
            commandLine(*commands(true), "--push")
        }

    }
